package vn.phuc.pscore.auction;

import vn.phuc.pscore.database.DatabaseManager;
import org.bukkit.inventory.ItemStack;
import java.sql.*;
import java.util.*;

public final class AuctionRepository {
    private final DatabaseManager database;
    public AuctionRepository(DatabaseManager database) { this.database = database; }

    public long create(UUID seller, String name, ItemStack item, double price) throws Exception {
        String sql = "INSERT INTO auctions(seller_uuid,seller_name,item,price,status,created_at) VALUES(?,?,?,?, 'ACTIVE',?)";
        try (PreparedStatement ps = database.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, seller.toString()); ps.setString(2, name);
            ps.setBytes(3, AuctionItemCodec.encode(item)); ps.setDouble(4, price);
            ps.setLong(5, System.currentTimeMillis()); ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) return keys.getLong(1); }
        }
        throw new SQLException("Khong tao duoc tin dang.");
    }

    public List<AuctionListing> findActive(String search, int limit, int offset) throws Exception {
        List<AuctionListing> out = new ArrayList<>();
        String sql = "SELECT * FROM auctions WHERE status='ACTIVE' ORDER BY created_at DESC LIMIT ? OFFSET ?";
        try (PreparedStatement ps = database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, Math.min(200, limit))); ps.setInt(2, Math.max(0, offset));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AuctionListing item = readListing(rs);
                    if (AuctionSearch.matches(search, item.item().getType().name(), item.sellerName())) out.add(item);
                }
            }
        }
        return out;
    }

    public List<AuctionListing> findMine(UUID owner) throws Exception {
        List<AuctionListing> out = new ArrayList<>();
        try (PreparedStatement ps = database.getConnection().prepareStatement("SELECT * FROM auctions WHERE seller_uuid=? AND status='ACTIVE' ORDER BY created_at DESC")) {
            ps.setString(1, owner.toString());
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) out.add(readListing(rs)); }
        }
        return out;
    }

    public AuctionListing get(long id) throws Exception {
        try (PreparedStatement ps = database.getConnection().prepareStatement("SELECT * FROM auctions WHERE id=?")) {
            ps.setLong(1, id); try (ResultSet rs = ps.executeQuery()) { return rs.next() ? readListing(rs) : null; }
        }
    }

    public boolean transition(long id, String from, String to) throws SQLException {
        try (PreparedStatement ps = database.getConnection().prepareStatement("UPDATE auctions SET status=? WHERE id=? AND status=?")) {
            ps.setString(1, to); ps.setLong(2, id); ps.setString(3, from); return ps.executeUpdate() == 1;
        }
    }

    public void addClaim(UUID owner, String source, String sourceId, ItemStack item) throws Exception {
        try (PreparedStatement ps = database.getConnection().prepareStatement("INSERT INTO item_claims(owner_uuid,source,source_id,item,created_at) VALUES(?,?,?,?,?)")) {
            ps.setString(1, owner.toString()); ps.setString(2, source); ps.setString(3, sourceId);
            ps.setBytes(4, AuctionItemCodec.encode(item)); ps.setLong(5, System.currentTimeMillis()); ps.executeUpdate();
        }
    }

    public List<Claim> claims(UUID owner) throws Exception {
        List<Claim> out = new ArrayList<>();
        try (PreparedStatement ps = database.getConnection().prepareStatement("SELECT id,item,source,source_id FROM item_claims WHERE owner_uuid=? AND claimed_at IS NULL ORDER BY id")) {
            ps.setString(1, owner.toString()); try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(new Claim(rs.getLong("id"), AuctionItemCodec.decode(rs.getBytes("item")), rs.getString("source"), rs.getString("source_id")));
            }
        }
        return out;
    }

    public boolean markClaimed(long id, UUID owner) throws SQLException {
        try (PreparedStatement ps = database.getConnection().prepareStatement("UPDATE item_claims SET claimed_at=? WHERE id=? AND owner_uuid=? AND claimed_at IS NULL")) {
            ps.setLong(1, System.currentTimeMillis()); ps.setLong(2, id); ps.setString(3, owner.toString()); return ps.executeUpdate() == 1;
        }
    }

    public void addHistory(AuctionListing listing, UUID buyer, String buyerName) throws Exception {
        try (PreparedStatement ps = database.getConnection().prepareStatement("INSERT INTO auction_history(auction_id,seller_uuid,seller_name,buyer_uuid,buyer_name,item,price,completed_at) VALUES(?,?,?,?,?,?,?,?)")) {
            ps.setLong(1, listing.id()); ps.setString(2, listing.sellerUuid().toString()); ps.setString(3, listing.sellerName());
            ps.setString(4, buyer.toString()); ps.setString(5, buyerName); ps.setBytes(6, AuctionItemCodec.encode(listing.item()));
            ps.setDouble(7, listing.price()); ps.setLong(8, System.currentTimeMillis()); ps.executeUpdate();
        }
    }

    public List<History> history(UUID owner) throws Exception {
        List<History> out = new ArrayList<>();
        String sql = "SELECT * FROM auction_history WHERE seller_uuid=? OR buyer_uuid=? ORDER BY completed_at DESC LIMIT 45";
        try (PreparedStatement ps = database.getConnection().prepareStatement(sql)) {
            ps.setString(1, owner.toString()); ps.setString(2, owner.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(new History(rs.getLong("auction_id"), rs.getString("seller_name"),
                        rs.getString("buyer_name"), AuctionItemCodec.decode(rs.getBytes("item")),
                        rs.getDouble("price"), rs.getLong("completed_at")));
            }
        }
        return out;
    }

    public record History(long auctionId, String seller, String buyer, ItemStack item, double price, long completedAt) {
        public History { item = item.clone(); }
        @Override public ItemStack item() { return item.clone(); }
    }

    public record Claim(long id, ItemStack item, String source, String sourceId) {
        public Claim { item = item.clone(); }
        @Override public ItemStack item() { return item.clone(); }
    }

    private AuctionListing readListing(ResultSet rs) throws Exception {
        return new AuctionListing(rs.getLong("id"), UUID.fromString(rs.getString("seller_uuid")),
                rs.getString("seller_name"), AuctionItemCodec.decode(rs.getBytes("item")),
                rs.getDouble("price"), rs.getString("status"), rs.getLong("created_at"));
    }
}
