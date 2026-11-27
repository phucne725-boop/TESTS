
package vn.phuc.pscore;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import vn.phuc.pscore.auction.AuctionModule;
import vn.phuc.pscore.command.CommandRegistrar;
import vn.phuc.pscore.database.DatabaseManager;
import vn.phuc.pscore.economy.EconomyService;
import vn.phuc.pscore.listener.PlayerListener;
import vn.phuc.pscore.player.PlayerDataService;

public final class PSCore extends JavaPlugin {

    private DatabaseManager database;
    private EconomyService economy;
    private PlayerDataService playerData;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        try {
            database = new DatabaseManager(this);
            database.initialize();

            economy = new EconomyService(this);
            economy.setup();

            playerData = new PlayerDataService(database);
            playerData.initialize();

            Bukkit.getPluginManager().registerEvents(
                    new PlayerListener(playerData), this
            );

            new AuctionModule(this).register();
            new CommandRegistrar(this).register();

            getLogger().info("PSCore da khoi dong thanh cong.");

            if (!economy.isAvailable()) {
                getLogger().warning(
                        "Chua co Vault Economy provider. Cac chuc nang tien te se chua hoat dong."
                );
            }
        } catch (Exception exception) {
            getLogger().severe("Khong the khoi dong PSCore.");
            exception.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (playerData != null) {
            playerData.saveAllOnlinePlayers();
        }

        if (database != null) {
            database.close();
        }

        getLogger().info("PSCore da tat.");
    }

    public DatabaseManager getDatabase() {
        return database;
    }

    public EconomyService getEconomy() {
        return economy;
    }

    public PlayerDataService getPlayerData() {
        return playerData;
    }
}
