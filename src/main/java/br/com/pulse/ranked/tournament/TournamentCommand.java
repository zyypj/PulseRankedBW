package br.com.pulse.ranked.tournament;

import com.tomkeuper.bedwars.api.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class TournamentCommand implements CommandExecutor {

    BedWars bAPI = Bukkit.getServicesManager().getRegistration(BedWars.class).getProvider();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("bw.tournament.create")) {
            sender.sendMessage("§cComando não encontrado ou você não tem permissão!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§6§lPulse §6tournament");
            sender.sendMessage("");
            sender.sendMessage("§6/tournament diamondGive §8§o<arena>");
            sender.sendMessage("§6/tournament pullPlayers §8§o<duplas/2v2> <jogadores>");
            return true;
        }

        if (args[0].equalsIgnoreCase("pullPlayers")) {

            if (args.length != 3) {
                sender.sendMessage("§cUse: /tournament pullPlayers <duplas/2v2> <jogadores>");
                return true;
            }

            String[] playersToPull = args[2].split(",");

            if (args[1].equalsIgnoreCase("duplas")) {
                for (String playerName : playersToPull) {
                    Player player = Bukkit.getPlayer(playerName);

                    if (player == null || !player.isOnline()) {
                        sender.sendMessage("§cO jogador " + playerName + " está offline. Jogadores não foram puxados.");
                        return true;
                    }
                }

                IArena arena = bAPI.getArenaUtil().getArenaByName("tournamentduplas1");

                for (String playerName : playersToPull) {
                    Player player = Bukkit.getPlayer(playerName);
                    if (player != null) {
                        arena.addPlayer(player, true);
                    }
                }
                return true;
            }

            if (args[1].equalsIgnoreCase("2v2")) {
                for (String playerName : playersToPull) {
                    Player player = Bukkit.getPlayer(playerName);

                    if (player == null || !player.isOnline()) {
                        sender.sendMessage("§cO jogador " + playerName + " está offline. Jogadores não foram puxados.");
                        return true;
                    }
                }

                IArena arena = bAPI.getArenaUtil().getArenaByName("tournament2v21");

                for (String playerName : playersToPull) {
                    Player player = Bukkit.getPlayer(playerName);
                    if (player != null) {
                        arena.addPlayer(player, true);
                    }
                }
                return true;
            }
            sender.sendMessage("§cUse /tournament pullPlayers <duplas/2v2> <jogadores>");
            return true;
        }

        if (args[0].equalsIgnoreCase("diamondGive")) {

            if (args.length != 2) {
                sender.sendMessage("§cUse: /tournament diamondGive <arena>");
                return true;
            }

            if (!args[1].equalsIgnoreCase("tournamentduplas1")) {
                sender.sendMessage("§cUse: /tournament diamondGive tournamentduplas1");
                return true;
            }

            IArena arena = bAPI.getArenaUtil().getArenaByName("tournamentduplas1");

            List<Player> players = arena.getPlayers();

            for (Player player : players) {
                Inventory playerInventory = player.getInventory();

                ItemStack itemStack = new ItemStack(Material.DIAMOND, 3);
                ItemMeta itemMeta = itemStack.getItemMeta();
                List<String> lore = Arrays.asList(
                        "",
                        "§7Use esse item em 20 segundos"
                );
                itemMeta.setLore(lore);
                itemMeta.setDisplayName("§cUse esse diamante em 20 segundos!");
                itemStack.setItemMeta(itemMeta);
                playerInventory.addItem(itemStack);

                player.sendMessage("");
                player.sendMessage("§e§lTodos os jogadores receberam");
                player.sendMessage("§b3 diamantes§e§l!");
                player.sendMessage("");

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        playerInventory.remove(itemStack);
                    }
                }.runTaskLater(Bukkit.getPluginManager().getPlugin("PulseRankedBW"), 15 * 20); // 15 * 20 ticks (15 seconds)
            }
            return true;
        }
        sender.sendMessage("§6§lPulse §6tournament");
        sender.sendMessage("");
        sender.sendMessage("§6/tournament diamondGive §8§o<arena>");
        sender.sendMessage("§6/tournament pullPlayers §8§o<duplas/2v2>");
        return true;
    }
}