package vn.phuc.pscore.auction;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import vn.phuc.pscore.util.MoneyParser;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class AuctionListener implements Listener {
    private final AuctionService service;
    private final AuctionMenu menu;
    private final Set<UUID> awaitingPrice = ConcurrentHashMap.newKeySet();
    private final Map<UUID, ItemStack[]> pendingItems = new ConcurrentHashMap<>();

    public AuctionListener(AuctionService service, AuctionMenu menu) { this.service = service; this.menu = menu; }

    @EventHandler public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getView().getTopInventory().getHolder() instanceof AuctionMenuHolder holder)) return;
        int raw = event.getRawSlot();
        if (raw < 0) return;
        if (holder.view() == AuctionMenuHolder.View.SELL) {
            if (raw >= event.getView().getTopInventory().getSize()) {
                if (event.isShiftClick()) event.setCancelled(true);
                return;
            }
            if (raw == 22) { event.setCancelled(true); confirmSell(player, event.getView().getTopInventory()); return; }
            if (raw == 26) { event.setCancelled(true); player.closeInventory(); return; }
            if (raw >= 18) event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        if (raw >= event.getView().getTopInventory().getSize()) return;
        try {
            switch (holder.view()) {
                case MARKET -> {
                    Long id = holder.listingSlots().get(raw);
                    if (id != null) {
                        if (service.buy(player, id)) { player.sendMessage(Component.text("Mua thanh cong. Vat pham da duoc dua vao Claim.", NamedTextColor.GREEN)); menu.openClaims(player); }
                        else player.sendMessage(Component.text("Tin dang khong con kha dung.", NamedTextColor.RED));
                    } else if (raw == 49) menu.openHub(player);
                    else if (raw == 47 && holder.page() > 0) menu.openMarket(player, holder.page()-1, holder.search());
                    else if (raw == 51) menu.openMarket(player, holder.page()+1, holder.search());
                    else if (raw == 53) player.closeInventory();
                    else if (raw == 45) { player.closeInventory(); player.sendMessage(Component.text("Nhap tu khoa tim kiem trong chat. Gui 'huy' de bo qua.")); player.setMetadata("ps_ah_search", new org.bukkit.metadata.FixedMetadataValue(Bukkit.getPluginManager().getPlugin("PSCore"), true)); }
                }
                case HUB -> {
                    if (raw == 10) menu.openSell(player);
                    else if (raw == 12) menu.openClaims(player);
                    else if (raw == 14) menu.openMine(player);
                    else if (raw == 16) menu.openHistory(player);
                    else if (raw == 22) menu.openMarket(player, 0, "");
                }
                case MINE -> {
                    Long id = holder.listingSlots().get(raw);
                    if (id != null) {
                        if (service.reclaim(player, id)) { player.sendMessage(Component.text("Da dua vat pham vao kho Claim.", NamedTextColor.GREEN)); menu.openMine(player); }
                        else player.sendMessage(Component.text("Khong the thu hoi tin dang nay.", NamedTextColor.RED));
                    } else if (raw == 49) menu.openHub(player);
                }
                case CLAIM -> {
                    Long id = holder.listingSlots().get(raw);
                    if (id != null && service.claim(player, id)) { player.sendMessage(Component.text("Da nhan vat pham.", NamedTextColor.GREEN)); menu.openClaims(player); }
                    else if (raw == 49) menu.openHub(player);
                }
                case HISTORY -> { if (raw == 49) menu.openHub(player); }
                default -> { }
            }
        } catch (Exception ex) { player.sendMessage(Component.text("Loi Auction House: " + ex.getMessage(), NamedTextColor.RED)); }
    }

    private void confirmSell(Player player, Inventory inventory) {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < 18; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && !item.getType().isAir()) { items.add(item.clone()); inventory.setItem(i, null); }
        }
        if (items.isEmpty()) { player.sendMessage(Component.text("Hay dat vat pham vao cac o trong.", NamedTextColor.RED)); return; }
        pendingItems.put(player.getUniqueId(), items.toArray(ItemStack[]::new));
        awaitingPrice.add(player.getUniqueId()); player.closeInventory();
        player.sendMessage(Component.text("Nhap gia ban (vd: 500, 10k, 2m). Gui 'huy' de huy.", NamedTextColor.YELLOW));
    }

    @EventHandler public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!awaitingPrice.contains(player.getUniqueId()) && !player.hasMetadata("ps_ah_search")) return;
        event.setCancelled(true); String message = event.getMessage();
        Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("PSCore"), () -> {
            try {
                if (player.hasMetadata("ps_ah_search")) {
                    player.removeMetadata("ps_ah_search", Bukkit.getPluginManager().getPlugin("PSCore"));
                    menu.openMarket(player, 0, message.equalsIgnoreCase("huy") ? "" : message); return;
                }
                awaitingPrice.remove(player.getUniqueId()); ItemStack[] items = pendingItems.remove(player.getUniqueId());
                if (items == null) return;
                if (message.equalsIgnoreCase("huy")) { giveBack(player, items); return; }
                double price = MoneyParser.parse(message);
                for (ItemStack item : items) service.list(player, item, price);
                player.sendMessage(Component.text("Da dang " + items.length + " tin ban.", NamedTextColor.GREEN));
                menu.openMine(player);
            } catch (Exception ex) {
                ItemStack[] items = pendingItems.remove(player.getUniqueId()); awaitingPrice.remove(player.getUniqueId());
                if (items != null) giveBack(player, items);
                player.sendMessage(Component.text("Khong dang ban duoc: " + ex.getMessage(), NamedTextColor.RED));
            }
        });
    }

    @EventHandler public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!(event.getView().getTopInventory().getHolder() instanceof AuctionMenuHolder holder)) return;
        if (holder.view() != AuctionMenuHolder.View.SELL) return;
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < 18; i++) { ItemStack item = event.getView().getTopInventory().getItem(i); if (item != null && !item.getType().isAir()) items.add(item.clone()); }
        if (!items.isEmpty()) Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("PSCore"), () -> giveBack(player, items.toArray(ItemStack[]::new)));
    }

    private void giveBack(Player player, ItemStack[] items) {
        for (ItemStack item : items) {
            Map<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
            leftovers.values().forEach(left -> player.getWorld().dropItemNaturally(player.getLocation(), left));
        }
    }
}
