package vn.phuc.pscore.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import vn.phuc.pscore.PSCore;
import vn.phuc.pscore.economy.EconomyService;
import vn.phuc.pscore.util.MoneyParser;
import vn.phuc.pscore.util.Text;

public final class PayCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private final EconomyService economy;

    public PayCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.economy = ((PSCore) plugin).getEconomy();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player payer)) {
            Text.sendPrefixed(plugin, sender, "Lenh nay chi danh cho nguoi choi.");
            return true;
        }
        if (args.length != 2) {
            Text.sendPrefixed(plugin, sender, "Su dung: /pay <player> <amount>");
            return true;
        }
        Player recipient = Bukkit.getPlayerExact(args[0]);
        if (recipient == null) {
            Text.sendPrefixed(plugin, sender, "Nguoi nhan phai dang truc tuyen.");
            return true;
        }
        if (recipient.getUniqueId().equals(payer.getUniqueId())) {
            Text.sendPrefixed(plugin, sender, "Ban khong the chuyen tien cho chinh minh.");
            return true;
        }

        final double amount;
        try {
            amount = MoneyParser.parse(args[1]);
        } catch (IllegalArgumentException exception) {
            Text.sendPrefixed(plugin, sender, exception.getMessage());
            return true;
        }

        try {
            if (!economy.has(payer, amount)) {
                Text.sendPrefixed(plugin, sender, "Ban khong du tien.");
                return true;
            }
            if (!economy.withdraw(payer, amount)) {
                Text.sendPrefixed(plugin, sender, "Khong the tru tien. Vui long thu lai.");
                return true;
            }
            if (!economy.deposit(recipient, amount)) {
                boolean refunded = economy.deposit(payer, amount);
                Text.sendPrefixed(plugin, sender, refunded
                        ? "Chuyen tien that bai; tien da duoc hoan lai."
                        : "LOI: Khong chuyen duoc tien va hoan tien that bai. Hay lien he quan tri vien.");
                if (!refunded) {
                    plugin.getLogger().severe("Pay rollback failed: " + payer.getUniqueId() + " -> " + recipient.getUniqueId());
                }
                return true;
            }
            String formatted = economy.format(amount);
            Text.sendPrefixed(plugin, payer, "Da chuyen &a" + formatted + " &fcho &e" + recipient.getName() + "&f.");
            Text.sendPrefixed(plugin, recipient, "Ban nhan duoc &a" + formatted + " &ftu &e" + payer.getName() + "&f.");
        } catch (IllegalStateException exception) {
            Text.sendPrefixed(plugin, sender, "He thong tien te chua san sang (can Vault va Economy provider).");
        }
        return true;
    }
}
