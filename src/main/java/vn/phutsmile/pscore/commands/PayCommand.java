
package vn.phutsmile.pscore.commands;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vn.phutsmile.pscore.PSCore;
import vn.phutsmile.pscore.utils.MoneyFormatter;

public final class PayCommand implements CommandExecutor {

    private final PSCore plugin;

    public PayCommand(PSCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
        CommandSender sender,
        Command command,
        String label,
        String[] args
    ) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (!player.hasPermission("pscore.pay")) {
            player.sendMessage("§cBạn không có quyền sử dụng lệnh này.");
            return true;
        }

        if (!plugin.getEconomyHook().isAvailable()) {
            sendMessage(player, "economy.messages.economy-unavailable");
            return true;
        }

        if (args.length != 2) {
            player.sendMessage("§cSử dụng: /pay <player> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);

        if (target == null) {
            sendMessage(player, "economy.messages.player-not-found");
            return true;
        }

        if (player.getUniqueId().equals(target.getUniqueId())) {
            sendMessage(player, "economy.messages.self-payment");
            return true;
        }

        double amount;

        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException exception) {
            sendMessage(player, "economy.messages.invalid-amount");
            return true;
        }

        if (!Double.isFinite(amount) || amount <= 0) {
            sendMessage(player, "economy.messages.invalid-amount");
            return true;
        }

        if (amount > 1_000_000_000_000_000D) {
            sendMessage(player, "economy.messages.invalid-amount");
            return true;
        }

        Economy economy = plugin.getEconomyHook().getEconomy();

        if (!economy.has(player, amount)) {
            sendMessage(player, "economy.messages.insufficient");
            return true;
        }

        EconomyResponse withdraw = economy.withdrawPlayer(player, amount);

        if (!withdraw.transactionSuccess()) {
            player.sendMessage("§cGiao dịch thất bại. Tiền chưa được chuyển.");
            plugin.getLogger().warning(
                "Withdraw failed for " + player.getName()
                + ": " + withdraw.errorMessage
            );
            return true;
        }

        EconomyResponse deposit = economy.depositPlayer(target, amount);

        if (!deposit.transactionSuccess()) {
            EconomyResponse refund = economy.depositPlayer(
                player,
                amount
            );

            player.sendMessage(
                refund.transactionSuccess()
                    ? "§cKhông thể nhận tiền bởi người nhận. Đã hoàn tiền."
                    : "§cLỖI NGHIÊM TRỌNG: Không thể hoàn tiền. Hãy liên hệ staff."
            );

            plugin.getLogger().severe(
                "Deposit failed for " + target.getName()
                + ": " + deposit.errorMessage
            );

            if (!refund.transactionSuccess()) {
                plugin.getLogger().severe(
                    "Refund failed for " + player.getName()
                    + ": " + refund.errorMessage
                );
            }

            return true;
        }

        String formatted = MoneyFormatter.formatWithSymbol(amount);

        sendMessage(
            player,
            "economy.messages.pay-success",
            "%amount%",
            formatted,
            "%player%",
            target.getName()
        );

        sendMessage(
            target,
            "economy.messages.pay-received",
            "%amount%",
            formatted,
            "%player%",
            player.getName()
        );

        return true;
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
            org.bukkit.ChatColor.translateAlternateColorCodes(
                '&',
                prefix + message
            )
        );
    }
}
