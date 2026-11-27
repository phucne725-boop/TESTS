package vn.phuc.pscore.auction;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;

public final class AuctionMenu {
    private final AuctionService service;
    public AuctionMenu(AuctionService service) { this.service = service; }

    public void openMarket(org.bukkit.entity.Player player, int page, String search) {
        AuctionMenuHolder holder = new AuctionMenuHolder(AuctionMenuHolder.View.MARKET, page, search);
        Inventory inv = Bukkit.createInventory(holder, 54, Component.text("PS Auction House", NamedTextColor.GOLD));
        try {
            List<AuctionListing> listings = service.market(search, page, 45);
            for (int i = 0; i < listings.size() && i < 45; i++) {
                AuctionListing listing = listings.get(i);
                ItemStack icon = listing.item(); ItemMeta meta = icon.getItemMeta();
                List<Component> lore = new ArrayList<>(meta.lore() == null ? List.of() : meta.lore());
                lore.add(Component.empty()); lore.add(Component.text("Gia: " + listing.price(), NamedTextColor.GREEN));
                lore.add(Component.text("Nguoi ban: " + listing.sellerName(), NamedTextColor.GRAY));
                lore.add(Component.text("ID: " + listing.id(), NamedTextColor.DARK_GRAY));
                lore.add(Component.text("Click de mua", NamedTextColor.YELLOW));
                meta.lore(lore); icon.setItemMeta(meta); inv.setItem(i, icon); holder.listingSlots().put(i, listing.id());
            }
        } catch (Exception ex) { player.sendMessage(Component.text("Loi tai chợ: " + ex.getMessage(), NamedTextColor.RED)); }
        inv.setItem(45, button(Material.COMPASS, "Tim kiem", "Click va nhap tu khoa trong chat"));
        inv.setItem(47, button(Material.ARROW, "Trang truoc", ""));
        inv.setItem(49, button(Material.CHEST, "Quan ly cua toi", "Ban vat pham, claim, tin dang, lich su"));
        inv.setItem(51, button(Material.ARROW, "Trang sau", ""));
        inv.setItem(53, button(Material.BARRIER, "Dong", ""));
        player.openInventory(inv);
    }

    public void openHub(org.bukkit.entity.Player player) {
        Inventory inv = Bukkit.createInventory(new AuctionMenuHolder(AuctionMenuHolder.View.HUB, 0, ""), 27, Component.text("Quan ly dau gia"));
        inv.setItem(10, button(Material.EMERALD, "Ban vat pham", "Mo giao dien dang ban"));
        inv.setItem(12, button(Material.CHEST, "Claim vat pham", "Nhan vat pham dang cho"));
        inv.setItem(14, button(Material.BOOK, "Tin dang cua toi", "Xem va thu hoi tin dang"));
        inv.setItem(16, button(Material.WRITABLE_BOOK, "Lich su giao dich", "Xem lich su mua ban"));
        inv.setItem(22, button(Material.ARROW, "Quay lai chợ", ""));
        player.openInventory(inv);
    }

    public void openSell(org.bukkit.entity.Player player) {
        Inventory inv = Bukkit.createInventory(new AuctionMenuHolder(AuctionMenuHolder.View.SELL, 0, ""), 27, Component.text("Dang ban vat pham"));
        inv.setItem(22, button(Material.EMERALD, "Xac nhan dang ban", "Dat vat pham vao o trong"));
        inv.setItem(26, button(Material.BARRIER, "Huy", "Tra lai vat pham"));
        player.openInventory(inv);
    }

    public void openMine(org.bukkit.entity.Player player) throws Exception {
        AuctionMenuHolder holder = new AuctionMenuHolder(AuctionMenuHolder.View.MINE, 0, "");
        Inventory inv = Bukkit.createInventory(holder, 54, Component.text("Tin dang cua toi"));
        List<AuctionListing> mine = service.mine(player.getUniqueId());
        for (int i = 0; i < Math.min(45, mine.size()); i++) {
            AuctionListing listing = mine.get(i); ItemStack icon = listing.item(); ItemMeta meta = icon.getItemMeta();
            meta.lore(List.of(Component.text("Gia: " + listing.price(), NamedTextColor.GREEN), Component.text("ID: " + listing.id()), Component.text("Click de thu hoi", NamedTextColor.YELLOW)));
            icon.setItemMeta(meta); inv.setItem(i, icon); holder.listingSlots().put(i, listing.id());
        }
        inv.setItem(49, button(Material.ARROW, "Quay lai", "")); player.openInventory(inv);
    }

    public void openClaims(org.bukkit.entity.Player player) throws Exception {
        AuctionMenuHolder holder = new AuctionMenuHolder(AuctionMenuHolder.View.CLAIM, 0, "");
        Inventory inv = Bukkit.createInventory(holder, 54, Component.text("Vat pham cho Claim"));
        List<AuctionRepository.Claim> claims = service.claims(player.getUniqueId());
        for (int i = 0; i < Math.min(45, claims.size()); i++) {
            AuctionRepository.Claim claim = claims.get(i); ItemStack icon = claim.item(); ItemMeta meta = icon.getItemMeta();
            meta.lore(List.of(Component.text("Claim ID: " + claim.id(), NamedTextColor.GRAY), Component.text("Click de nhan", NamedTextColor.GREEN)));
            icon.setItemMeta(meta); inv.setItem(i, icon); holder.listingSlots().put(i, claim.id());
        }
        inv.setItem(49, button(Material.ARROW, "Quay lai", "")); player.openInventory(inv);
    }

    public void openHistory(org.bukkit.entity.Player player) throws Exception {
        Inventory inv = Bukkit.createInventory(new AuctionMenuHolder(AuctionMenuHolder.View.HISTORY, 0, ""), 54, Component.text("Lich su giao dich"));
        List<AuctionRepository.History> history = service.history(player.getUniqueId());
        for (int i = 0; i < Math.min(45, history.size()); i++) {
            AuctionRepository.History entry = history.get(i); ItemStack icon = entry.item(); ItemMeta meta = icon.getItemMeta();
            meta.lore(List.of(Component.text("Nguoi ban: " + entry.seller()), Component.text("Nguoi mua: " + entry.buyer()),
                    Component.text("Gia: " + entry.price(), NamedTextColor.GREEN), Component.text("Ma tin: " + entry.auctionId())));
            icon.setItemMeta(meta); inv.setItem(i, icon);
        }
        inv.setItem(49, button(Material.ARROW, "Quay lai", "")); player.openInventory(inv);
    }

    private ItemStack button(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material); ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, NamedTextColor.YELLOW));
        if (lore != null && !lore.isBlank()) meta.lore(List.of(Component.text(lore, NamedTextColor.GRAY)));
        item.setItemMeta(meta); return item;
    }
}
