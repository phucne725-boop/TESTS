
package vn.phutsmile.pscore.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import vn.phutsmile.pscore.PSCore;

public final class EconomyHook {

    private final PSCore plugin;
    private Economy economy;

    public EconomyHook(PSCore plugin) {
        this.plugin = plugin;
    }

    public boolean setup() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault is not installed.");
            return false;
        }

        RegisteredServiceProvider<Economy> registration =
            plugin.getServer()
                .getServicesManager()
                .getRegistration(Economy.class);

        if (registration == null) {
            plugin.getLogger().warning(
                "No Economy provider registered with Vault."
            );
            return false;
        }

        economy = registration.getProvider();

        if (economy == null) {
            plugin.getLogger().warning("Vault Economy provider is null.");
            return false;
        }

        plugin.getLogger().info(
            "Economy hooked: " + economy.getName()
        );

        return true;
    }

    public boolean isAvailable() {
        return economy != null;
    }

    public Economy getEconomy() {
        return economy;
    }
}
