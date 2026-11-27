
package vn.phuc.pscore.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class Text {

    private static final LegacyComponentSerializer SERIALIZER =
            LegacyComponentSerializer.legacyAmpersand();

    private Text() {
    }

    public static Component component(String message) {
        return SERIALIZER.deserialize(message == null ? "" : message);
    }

    public static void send(CommandSender sender, String message) {
        sender.sendMessage(component(message));
    }

    public static void sendPrefixed(
            JavaPlugin plugin,
            CommandSender sender,
            String message
    ) {
        String prefix = plugin.getConfig().getString(
                "general.prefix", "&b&lPSCore &8»"
        );

        send(sender, prefix + " &f" + message);
    }
}
