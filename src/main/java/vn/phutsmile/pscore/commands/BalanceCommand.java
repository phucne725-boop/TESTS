
package vn.phutsmile.pscore.commands;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vn.phutsmile.pscore.PSCore;
import vn.phutsmile.pscore.utils.MoneyFormatter;

public final class BalanceCommand implements CommandExecutor {

    private final PSCore plugin;

    public BalanceCommand(PSCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
        CommandSender sender,
        Command command,
        String label,
        String[] args
    ) {
        if (!plugin.getEconomyHook().isAvailable()) {
            sendMessage(sender, "economy.messages.economy-unavailable");
            return true;
        }

        Economy economy = plugin.getEconomyHook().getEconomy();

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(
                    "Console must specify a player: /bal <player>"
                );
                return true;
            }

            double balance = economy.getBalance(player);

            sendMessage(
                player,
                "economy.messages.balance",
                "%money%",
                MoneyFormatter.formatWithSymbol(balance)
            );

            return true;
        }

        if (args.length == 1) {
            if (!sender.hasPermission("pscore.balance.others")) {
                sender.sendMessage("§cBạn không có quyền xem số dư người khác.");
                return true;
            }

            OfflinePlayer target = findPlayer(args[0]);

            if (target == null) {
                sendMessage(sender, "economy.messages.player-not-found");
                return true;
            }

            double balance = economy.getBalance(target);

            sendMessage(
                sender,
                "economy.messages.balance-other",
                "%player%",
                target.getName() == null ? args[0] : target.getName(),
                "%money%",
                MoneyFormatter.formatWithSymbol(balance)
            );

            return true;
        }

        sender.sendMessage("§cSử dụng: /bal [player]");
        return true;
    }

    private OfflinePlayer findPlayer(String name) {
        Player online = Bukkit.getPlayerExact(name);

        if (online != null) {
            return online;
        }

        OfflinePlayer offline = Bukkit.getOfflinePlayer(name);

        if (!offline.hasPlayedBefore()) {
            return null;
        }

        return offline;
    }

    private void sendMessage(
        CommandSender sender,
        String path,
        String... replacements
    ) {
        String message = plugin.getConfig().getString(
            path,
            "&cMessage not configured."
        );

        for (int i = 0; i + 1 < replacements.length; i += 2) {
            message = message.replace(
                replacements[i],
                replacements[i + 1]
            );
        }

        String prefix = plugin.getConfig().getString(
            "economy.messages.prefix",
            ""
        );

        sender.sendMessage(
            color(prefix + message)
        );
    }

    private String color(String message) {
        return org.bukkit.ChatColor.translateAlternateColorCodes(
            '&',
            message
        );
    }
}
