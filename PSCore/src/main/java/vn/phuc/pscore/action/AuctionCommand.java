package vn.phuc.pscore.auction;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.*;

public final class AuctionCommand implements CommandExecutor, TabCompleter {
    private final AuctionMenu menu;
    public AuctionCommand(AuctionMenu menu) { this.menu = menu; }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Lenh nay chi danh cho nguoi choi."); return true; }
        try {
            if (args.length == 0) { menu.openMarket(player, 0, ""); return true; }
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "mine" -> menu.openMine(player);
                case "sell" -> menu.openSell(player);
                case "search" -> menu.openMarket(player, 0, args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "");
                case "help" -> {
                    player.sendMessage(Component.text("/ah - Mo cho dau gia", NamedTextColor.YELLOW));
                    player.sendMessage(Component.text("/ah mine - Tin dang cua ban", NamedTextColor.YELLOW));
                    player.sendMessage(Component.text("/ah sell - Dang ban vat pham", NamedTextColor.YELLOW));
                    player.sendMessage(Component.text("/ah search <tu khoa> - Tim kiem", NamedTextColor.YELLOW));
                }
                default -> menu.openMarket(player, 0, String.join(" ", args));
            }
        } catch (Exception ex) { player.sendMessage(Component.text("Loi Auction House: " + ex.getMessage(), NamedTextColor.RED)); }
        return true;
    }

    @Override public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("mine", "sell", "search", "help");
        return Collections.emptyList();
    }
}
