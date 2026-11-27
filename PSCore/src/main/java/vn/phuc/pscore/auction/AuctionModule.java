package vn.phuc.pscore.auction;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class AuctionModule {
    private final JavaPlugin plugin;
    public AuctionModule(JavaPlugin plugin) { this.plugin = plugin; }

    public void register() {
        if (!plugin.getConfig().getBoolean("auction.enabled", true)) {
            plugin.getLogger().info("Auction House dang tat trong config.yml.");
            return;
        }
        AuctionRepository repository = new AuctionRepository(
                ((vn.phuc.pscore.PSCore) plugin).getDatabase()
        );
        AuctionService service = new AuctionService(
                repository, ((vn.phuc.pscore.PSCore) plugin).getEconomy()
        );
        AuctionMenu menu = new AuctionMenu(service);
        plugin.getServer().getPluginManager().registerEvents(new AuctionListener(service, menu), plugin);
        PluginCommand command = plugin.getCommand("ah");
        if (command == null) throw new IllegalStateException("Thieu khai bao lenh ah trong plugin.yml");
        AuctionCommand executor = new AuctionCommand(menu);
        command.setExecutor(executor);
        command.setTabCompleter(executor);
        plugin.getLogger().info("Auction House da duoc dang ky.");
    }
}
