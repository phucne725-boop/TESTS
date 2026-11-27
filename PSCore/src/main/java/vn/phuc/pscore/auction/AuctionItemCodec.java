package vn.phuc.pscore.auction;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import java.io.*;

public final class AuctionItemCodec {
    private AuctionItemCodec() {}

    public static byte[] encode(ItemStack item) throws IOException {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             BukkitObjectOutputStream out = new BukkitObjectOutputStream(bytes)) {
            out.writeObject(item.clone());
            out.flush();
            return bytes.toByteArray();
        }
    }

    public static ItemStack decode(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bytes = new ByteArrayInputStream(data);
             BukkitObjectInputStream in = new BukkitObjectInputStream(bytes)) {
            Object value = in.readObject();
            if (!(value instanceof ItemStack item)) throw new IOException("Du lieu vat pham khong hop le.");
            return item;
        }
    }

}
