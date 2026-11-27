package vn.phuc.pscore.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import vn.phuc.pscore.economy.EconomyService;
import vn.phuc.pscore.util.Text;

public final class BalanceCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private final EconomyService economy;

    public BalanceCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.economy = ((vn.phuc.pscore.PSCore) plugin).getEconomy();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 1) {
            Text.sendPrefixed(plugin, sender, "Su dung: /bal [player]");
            return true;
        }
        OfflinePlayer target;
        if (args.length == 1) {
            target = Bukkit.getOfflinePlayer(args[0]);
            if (!target.hasPlayedBefore() && !target.isOnline()) {
                Text.sendPrefixed(plugin, sender, "Khong tim thay nguoi choi nay.");
                return true;
            }
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            Text.sendPrefixed(plugin, sender, "Console can dung /bal <player>.");
            return true;
        }
        try {
            Text.sendPrefixed(plugin, sender,
                    "So du cua " + (target.getName() == null ? "Unknown" : target.getName())
                            + ": &a" + economy.format(economy.getBalance(target)));
        } catch (IllegalStateException exception) {
            Text.sendPrefixed(plugin, sender, "He thong tien te chua san sang (can Vault va Economy provider).");
        }
        return true;
    }
}
