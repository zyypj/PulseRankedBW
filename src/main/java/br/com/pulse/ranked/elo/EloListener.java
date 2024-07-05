package br.com.pulse.ranked.elo;

import br.com.pulse.ranked.Main;
import com.tomkeuper.bedwars.api.BedWars;
import com.tomkeuper.bedwars.api.arena.shop.ICategoryContent;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.events.gameplay.GameEndEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerBedBreakEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerKillEvent;
import com.tomkeuper.bedwars.api.events.shop.ShopBuyEvent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class EloListener implements Listener {

    private final EloManager eloManager;
    private final Main plugin;
    private final FileConfiguration playerData;
    BedWars bedwarsAPI = Bukkit.getServicesManager().getRegistration(BedWars.class).getProvider();

    public EloListener(EloManager eloManager, Main plugin, FileConfiguration playerData) {
        this.eloManager = eloManager;
        this.plugin = plugin;
        this.playerData = playerData;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!playerData.contains(uuid.toString())) {
            playerData.set(uuid.toString(), 0);
            eloManager.savePlayerData();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        eloManager.savePlayerData();
    }

    //eventos de jogo
    @EventHandler
    public void playerBedBreak(PlayerBedBreakEvent e) {
        Player player = e.getPlayer();
        UUID playerUUID = player.getUniqueId();
        if (bedwarsAPI.getArenaUtil().isPlaying(player)) {
            String group = e.getArena().getGroup();
            if (group.equalsIgnoreCase("RankedSolo") || group.equalsIgnoreCase("RankedDuplas") ||group.equalsIgnoreCase("Ranked1v1")) {
                Random random = new Random();
                int playerEloIncrease = random.nextInt(13) + 4; // Gera um número aleatório de 4 a 16
                eloManager.addElo(playerUUID, playerEloIncrease, group.toLowerCase());
                player.sendMessage("§c+" + playerEloIncrease + " Ranked Elo (Quebra de Cama)");
            }
        }
    }

    @EventHandler
    public void onKill(PlayerKillEvent e) {
        Player killer = e.getKiller();
        UUID killerUUID = killer.getUniqueId();
        if (killerUUID != null) {
            if (bedwarsAPI.getArenaUtil().isPlaying(killer)) {
                String group = e.getArena().getGroup();
                if (group.equalsIgnoreCase("RankedSolo") || group.equalsIgnoreCase("RankedDuplas") || group.equalsIgnoreCase("Ranked1v1")) {
                    if (e.getCause().isFinalKill()) {
                        Random random = new Random();
                        int killerEloIncrease = random.nextInt(8) + 1; // Gera um número aleatório de 1 a 8
                        eloManager.addElo(killerUUID, killerEloIncrease, group.toLowerCase());
                        killer.sendMessage("§c+" + killerEloIncrease + " Ranked Elo (Kill Final)");
                        killer.playSound(killer.getLocation(), Sound.LEVEL_UP, 1, 1);

                    }
                }
            }
        }
    }

    @EventHandler
    public void gameEnd(GameEndEvent e) {
        String group = e.getArena().getGroup();
        ITeam winnerTeam = e.getTeamWinner();
        List<UUID> loserTeam = e.getLosers();
        if (group.equalsIgnoreCase("RankedSolo") || group.equalsIgnoreCase("RankedDuplas") || group.equalsIgnoreCase("Ranked1v1")) {
            for (Player winner : winnerTeam.getMembers()) {
                Random random = new Random();
                int winnerEloIncrease = random.nextInt(11) + 10; // Gera um número aleatório de 10 a 20
                eloManager.addElo(winner.getUniqueId(), winnerEloIncrease, group.toLowerCase());
                winner.sendMessage("§c+" + winnerEloIncrease + " Ranked Elo (Vitória)");
                winner.playSound(winner.getLocation(), Sound.LEVEL_UP, 1, 1);
            }
            for (UUID loserUUID : loserTeam) {
                Player loser = Bukkit.getPlayer(loserUUID);
                Random random = new Random();
                int loserEloPerca = random.nextInt(21) + 10; // Gera um número aleatório de 20 a 30
                eloManager.addElo(loser.getUniqueId(), loserEloPerca, group.toLowerCase());
                loser.sendMessage("§c-" + -loserEloPerca + " Ranked Elo (Derrota)");
                loser.playSound(loser.getLocation(), Sound.LEVEL_UP, 1, 1);
            }
        }
    }

    @EventHandler
    public void onShop(ShopBuyEvent e) {
        String group = e.getArena().getGroup();
        Player player = e.getBuyer();
        ITeam teamA = e.getArena().getTeam("Azul");
        ITeam teamB = e.getArena().getTeam("Vermelho");
        ICategoryContent categoryContent = e.getCategoryContent();
        String identifier = categoryContent.getIdentifier();
        // Permanently blocked items
        boolean b = group.equalsIgnoreCase("RankedSolo") || group.equalsIgnoreCase("RankedDuplas") || group.equalsIgnoreCase("Ranked1v1") || group.equalsIgnoreCase("Ranked4s");
        if (b) {
            if (identifier.equals("ranged-category.category-content.bow1") ||
                    identifier.equals("ranged-category.category-content.arrow") ||
                    identifier.equals("ranged-category.category-content.bow2") ||
                    identifier.equals("ranged-category.category-content.bow3") ||
                    identifier.equals("utility-category.category-content.ender-pearl") ||
                    identifier.equals("shop-specials.tower") ||
                    identifier.equals("blocks-category.category-content.obsidian") ||
                    identifier.equals("utility-category.category-content.tower")) {
                player.sendMessage("§cItem bloqueado nos modos ranqueados!");
                e.setCancelled(true);
                return;
            }
        }
        // Temporarily blocked items
        if (group.equalsIgnoreCase("Ranked1v1") || group.equalsIgnoreCase("Ranked4s")) {
            if (!teamB.isBedDestroyed() || !teamA.isBedDestroyed()) {
                if (identifier.equals("potions-category.category-content.invisibility") ||
                        identifier.equals("potions-category.category-content.jump-potion") ||
                        identifier.equals("potions-category.category-content.speed-potion") ||
                        identifier.equals("utility-category.category-content.tnt") ||
                        identifier.equals("utility-category.category-content.water-bucket") ||
                        identifier.equals("shop-specials.iron-golem") ||
                        identifier.equals("utility-category.category-content.bedbug") ||
                        identifier.equals("melee-category.category-content.stick") ||
                        identifier.equals("utility-category.category-content.dream-defender")) {
                    player.sendMessage("§cItem bloqueado até alguma cama for quebrada!");
                    e.setCancelled(true);
                }
            }
        }

        if (b) {
            if (e.getArena().getUpgradeDiamondsCount() != 2) {
                Bukkit.getConsoleSender().sendMessage(String.valueOf(e.getArena().getUpgradeDiamondsCount()));
                if (identifier.equals("utility-category.category-content.bridge-egg")) {
                    player.sendMessage("§cItem bloqueado até o Diamante 3!");
                    e.setCancelled(true);
                }
            }
        }
    }
}

