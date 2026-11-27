package vn.phuc.pscore.auction;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import java.util.HashMap;
import java.util.Map;

public final class AuctionMenuHolder implements InventoryHolder {
    public enum View { MARKET, HUB, SELL, MINE, CLAIM, HISTORY }
    private final View view;
    private int page;
    private String search = "";
    private final Map<Integer, Long> listingSlots = new HashMap<>();
    public AuctionMenuHolder(View view, int page, String search) {
        this.view = view; this.page = page; this.search = search == null ? "" : search;
    }
    public View view() { return view; }
    public int page() { return page; }
    public void page(int page) { this.page = Math.max(0, page); }
    public String search() { return search; }
    public void search(String search) { this.search = search == null ? "" : search; }
    public Map<Integer, Long> listingSlots() { return listingSlots; }
    @Override public Inventory getInventory() { return null; }
}
