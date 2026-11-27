package vn.phuc.pscore.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import vn.phuc.pscore.role.RoleService;
import vn.phuc.pscore.role.RoleType;
import vn.phuc.pscore.util.Text;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class RoleCommand implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    private final RoleService roles;

    public RoleCommand(JavaPlugin plugin, RoleService roles) {
        this.plugin = plugin;
        this.roles = roles;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("pscore.staffrole")) {
            Text.sendPrefixed(plugin, sender, "Ban khong co quyen dung lenh nay.");
            return true;
        }
        boolean remove = command.getName().equalsIgnoreCase("staffunrole");
        if (remove) {
            if (args.length != 1) {
                Text.sendPrefixed(plugin, sender, "Su dung: /staffunrole <player>");
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            try {
                roles.removeRole(target.getUniqueId());
                Text.sendPrefixed(plugin, sender, "Da xoa vai tro cua &e" + args[0] + "&f.");
            } catch (SQLException exception) {
                Text.sendPrefixed(plugin, sender, "Khong the cap nhat database.");
                plugin.getLogger().severe("staffunrole failed: " + exception.getMessage());
            }
            return true;
        }

        if (args.length < 2 || args.length > 3) {
            Text.sendPrefixed(plugin, sender, "Su dung: /staffrole <player> <ADMIN|STAFF|MEDIA|SPECTATOR|SYSTEM> [minutes]");
            return true;
        }
        RoleType role = RoleType.from(args[1]).orElse(null);
        if (role == null) {
            Text.sendPrefixed(plugin, sender, "Role khong hop le. Cac role: ADMIN, STAFF, MEDIA, SPECTATOR, SYSTEM.");
            return true;
        }
        long expiresAt = 0L;
        if (args.length == 3) {
            try {
                long minutes = Long.parseLong(args[2]);
                if (minutes <= 0) throw new NumberFormatException();
                expiresAt = Math.addExact(System.currentTimeMillis(), Math.multiplyExact(minutes, 60_000L));
            } catch (ArithmeticException | NumberFormatException exception) {
                Text.sendPrefixed(plugin, sender, "So phut khong hop le (phai la so nguyen duong).");
                return true;
            }
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        try {
            roles.setRole(target, role, expiresAt);
            String duration = expiresAt == 0 ? "vo thoi han" : (args[2] + " phut");
            Text.sendPrefixed(plugin, sender, "Da gan role &e" + role.name() + " &fcho &e" + args[0] + " &f(" + duration + ").");
        } catch (SQLException exception) {
            Text.sendPrefixed(plugin, sender, "Khong the luu role vao database.");
            plugin.getLogger().severe("staffrole failed: " + exception.getMessage());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("pscore.staffrole")) return List.of();
        if (command.getName().equalsIgnoreCase("staffunrole")) return List.of();
        if (args.length == 2) {
            String prefix = args[1].toUpperCase(Locale.ROOT);
            List<String> matches = new ArrayList<>();
            for (RoleType role : RoleType.values()) {
                if (role.name().startsWith(prefix)) matches.add(role.name());
            }
            return matches;
        }
        return List.of();
    }
}
