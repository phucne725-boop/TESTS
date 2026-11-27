
package vn.phuc.pscore.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class EconomyService {

    private final JavaPlugin plugin;
    private Economy provider;

    public EconomyService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            plugin.getLogger().warning("Khong tim thay Vault.");
            return;
        }

        RegisteredServiceProvider<Economy> registration =
                Bukkit.getServicesManager().getRegistration(Economy.class);

        if (registration != null) {
            provider = registration.getProvider();
        }
    }

    public boolean isAvailable() {
        return provider != null;
    }

    public double getBalance(OfflinePlayer player) {
        requireProvider();
        return provider.getBalance(player);
    }

    public boolean has(OfflinePlayer player, double amount) {
        requireProvider();
        return Double.isFinite(amount)
                && amount >= 0
                && provider.has(player, amount);
    }

    public boolean withdraw(OfflinePlayer player, double amount) {
        requireProvider();

        if (!validAmount(amount) || !provider.has(player, amount)) {
            return false;
        }

        return provider.withdrawPlayer(player, amount)
                .transactionSuccess();
    }

    public boolean deposit(OfflinePlayer player, double amount) {
        requireProvider();

        if (!validAmount(amount)) {
            return false;
        }

        return provider.depositPlayer(player, amount)
                .transactionSuccess();
    }

    public String format(double amount) {
        requireProvider();
        return provider.format(amount);
    }

    private boolean validAmount(double amount) {
        return Double.isFinite(amount) && amount > 0;
    }

    private void requireProvider() {
        if (provider == null) {
            throw new IllegalStateException(
                    "Khong co Vault Economy provider."
            );
        }
    }
}
