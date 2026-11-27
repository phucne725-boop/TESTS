package vn.phuc.pscore.auction;

import org.bukkit.inventory.ItemStack;
import java.util.UUID;

public record AuctionListing(
        long id, UUID sellerUuid, String sellerName, ItemStack item,
        double price, String status, long createdAt
) {
    public AuctionListing {
        item = item.clone();
    }
    @Override public ItemStack item() { return item.clone(); }
}
