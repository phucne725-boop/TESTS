package vn.phuc.pscore.command;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import vn.phuc.pscore.PSCore;
import vn.phuc.pscore.role.RoleService;
import vn.phuc.pscore.scoreboard.PlayerPresentationListener;
import vn.phuc.pscore.scoreboard.ScoreboardService;

/** Registers the non-auction commands and player presentation systems. */
public final class CommandRegistrar {
    private final PSCore plugin;

    public CommandRegistrar(JavaPlugin plugin) {
        if (!(plugin instanceof PSCore core)) {
            throw new IllegalArgumentException("CommandRegistrar requires PSCore.");
        }
        this.plugin = core;
    }

    public void register() {
        RoleService roles = new RoleService(plugin);
        roles.load();

        registerCommand("bal", new BalanceCommand(plugin));
        registerCommand("pay", new PayCommand(plugin));
        registerCommand("stats", new StatsCommand(plugin));

        RoleCommand roleCommand = new RoleCommand(plugin, roles);
        registerCommand("staffrole", roleCommand);
        registerCommand("staffunrole", roleCommand);

        ScoreboardService scoreboard = new ScoreboardService(
                plugin, plugin.getPlayerData(), plugin.getEconomy(), roles
        );
        plugin.getServer().getPluginManager().registerEvents(
                new PlayerPresentationListener(plugin, roles), plugin
        );
        scoreboard.start();
        plugin.getLogger().info("Command va he thong hien thi da duoc dang ky.");
    }

    private void registerCommand(String name, Object executor) {
        PluginCommand command = plugin.getCommand(name);
        if (command == null) {
            throw new IllegalStateException("Thieu lenh '" + name + "' trong plugin.yml");
        }
        if (executor instanceof org.bukkit.command.CommandExecutor commandExecutor) {
            command.setExecutor(commandExecutor);
        }
        if (executor instanceof org.bukkit.command.TabCompleter tabCompleter) {
            command.setTabCompleter(tabCompleter);
        }
    }
}
