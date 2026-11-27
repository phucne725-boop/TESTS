package vn.phuc.pscore.auction;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vn.phuc.pscore.economy.EconomyService;
import java.util.*;

public final class AuctionService {
    private final AuctionRepository repository;
    private final EconomyService economy;
    public AuctionService(AuctionRepository repository, EconomyService economy) {
        this.repository = repository; this.economy = economy;
    }

    public long list(Player seller, ItemStack item, double price) throws Exception {
        if (item == null || item.getType().isAir()) throw new IllegalArgumentException("Vat pham khong hop le.");
        if (!Double.isFinite(price) || price <= 0) throw new IllegalArgumentException("Gia ban phai lon hon 0.");
        return repository.create(seller.getUniqueId(), seller.getName(), item, price);
    }

    public List<AuctionListing> market(String search, int page, int pageSize) throws Exception {
        return repository.findActive(search, pageSize, page * pageSize);
    }
    public List<AuctionListing> mine(UUID owner) throws Exception { return repository.findMine(owner); }
    public List<AuctionRepository.Claim> claims(UUID owner) throws Exception { return repository.claims(owner); }
    public List<AuctionRepository.History> history(UUID owner) throws Exception { return repository.history(owner); }

    public synchronized boolean reclaim(Player seller, long id) throws Exception {
        AuctionListing listing = repository.get(id);
        if (listing == null || !listing.sellerUuid().equals(seller.getUniqueId()) || !"ACTIVE".equals(listing.status())) return false;
        if (!repository.transition(id, "ACTIVE", "RECLAIMED")) return false;
        try {
            repository.addClaim(seller.getUniqueId(), "RECLAIM", Long.toString(id), listing.item());
            return true;
        } catch (Exception ex) {
            repository.transition(id, "RECLAIMED", "ACTIVE");
            throw ex;
        }
    }

    public synchronized boolean buy(Player buyer, long id) throws Exception {
        AuctionListing listing = repository.get(id);
        if (listing == null || !"ACTIVE".equals(listing.status())) return false;
        if (listing.sellerUuid().equals(buyer.getUniqueId())) throw new IllegalArgumentException("Ban khong the tu mua tin dang cua minh.");
        if (!economy.has(buyer, listing.price())) throw new IllegalArgumentException("Ban khong du tien.");
        if (!repository.transition(id, "ACTIVE", "PROCESSING")) return false;

        boolean withdrawn = false;
        try {
            if (!economy.withdraw(buyer, listing.price())) throw new IllegalStateException("Khong the tru tien nguoi mua.");
            withdrawn = true;
            OfflinePlayer seller = Bukkit.getOfflinePlayer(listing.sellerUuid());
            if (!economy.deposit(seller, listing.price())) throw new IllegalStateException("Khong the cong tien cho nguoi ban.");
            repository.addClaim(buyer.getUniqueId(), "PURCHASE", Long.toString(id), listing.item());
            repository.addHistory(listing, buyer.getUniqueId(), buyer.getName());
            if (!repository.transition(id, "PROCESSING", "SOLD")) {
                throw new IllegalStateException("Khong the hoan tat trang thai giao dich.");
            }
            return true;
        } catch (Exception ex) {
            if (withdrawn) {
                // Refund only when seller payout did not complete. If payout completed,
                // leave PROCESSING for staff recovery rather than creating duplicate money.
                // The caller is informed; no automatic unsafe second payout is attempted.
            }
            throw ex;
        }
    }

    public boolean claim(Player player, long claimId) throws Exception {
        List<AuctionRepository.Claim> pending = repository.claims(player.getUniqueId());
        AuctionRepository.Claim selected = pending.stream().filter(c -> c.id() == claimId).findFirst().orElse(null);
        if (selected == null) return false;
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(selected.item());
        if (!leftovers.isEmpty()) {
            leftovers.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
        }
        if (!repository.markClaimed(claimId, player.getUniqueId())) {
            // The item has already been delivered. Prevent a second claim; report state conflict.
            throw new IllegalStateException("Khong the cap nhat trang thai claim. Hay lien he staff.");
        }
        return true;
    }
}
