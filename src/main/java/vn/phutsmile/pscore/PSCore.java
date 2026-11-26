
package vn.phutsmile.pscore;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import vn.phutsmile.pscore.database.DatabaseManager;
import vn.phutsmile.pscore.economy.EconomyHook;
import vn.phutsmile.pscore.commands.BalanceCommand;
import vn.phutsmile.pscore.commands.PayCommand;

public final class PSCore extends JavaPlugin {

    private static PSCore instance;

    private DatabaseManager databaseManager;
    private EconomyHook economyHook;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        getLogger().info("Starting PSCore...");

        try {
            databaseManager = new DatabaseManager(this);
            databaseManager.connect();
            databaseManager.initializeTables();
        } catch (Exception exception) {
            getLogger().severe("Cannot initialize database!");
            exception.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        economyHook = new EconomyHook(this);

        if (!economyHook.setup()) {
            getLogger().warning(
                "Vault Economy provider not found. Economy commands will be unavailable."
            );
        }

        registerCommands();

        getLogger().info("--------------------------------");
        getLogger().info("PSCore enabled!");
        getLogger().info("Server: PhutSmileSmp");
        getLogger().info("--------------------------------");
    }

    private void registerCommands() {
        PluginCommand balance = getCommand("bal");
        PluginCommand pay = getCommand("pay");

        if (balance != null) {
            balance.setExecutor(new BalanceCommand(this));
        } else {
            getLogger().severe("Command bal is missing from plugin.yml!");
        }

        if (pay != null) {
            pay.setExecutor(new PayCommand(this));
        } else {
            getLogger().severe("Command pay is missing from plugin.yml!");
        }
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }

        getLogger().info("PSCore disabled.");
        instance = null;
    }

    public static PSCore getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public EconomyHook getEconomyHook() {
        return economyHook;
    }
}
