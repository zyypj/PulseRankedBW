package br.com.pulse.ranked.misc.mvp;

import br.com.pulse.ranked.Main;
import br.com.pulse.ranked.elo.EloManager;
import com.tomkeuper.bedwars.api.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.events.gameplay.GameEndEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerBedBreakEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerKillEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class MVPListener implements Listener {

    private final Main plugin;
    private final MVPManager mvpManager;
    private final EloManager eloManager;

    BedWars bedwarsAPI = Bukkit.getServicesManager().getRegistration(BedWars.class).getProvider();

    public MVPListener(Main plugin, MVPManager mvpManager, EloManager eloManager) {
        this.plugin = plugin;
        this.mvpManager = mvpManager;
        this.eloManager = eloManager;
    }

    @EventHandler
    public void playerKill(PlayerKillEvent e) {
        Player killer = e.getKiller();
        if (bedwarsAPI.getArenaUtil().isPlaying(killer)) {
            IArena arena = e.getArena();
            String group = arena.getGroup();
            if (group.equalsIgnoreCase("RankedSolo") || group.equalsIgnoreCase("RankedDuplas") || group.equalsIgnoreCase("Ranked1v1") || group.equalsIgnoreCase("Ranked4s")) {
                if (e.getCause().isFinalKill()) {
                    mvpManager.addFinalKillsPoints(killer, arena);
                }
            }
        }
    }

    @EventHandler
    public void onBedBreaking(PlayerBedBreakEvent e) {
        IArena arena = e.getArena();
        String group = arena.getGroup();
        if (group.equalsIgnoreCase("RankedSolo") || group.equalsIgnoreCase("RankedDuplas")
                || group.equalsIgnoreCase("Ranked1v1") || group.equalsIgnoreCase("Ranked4s")) {
            Player player = e.getPlayer();
            if (player != null) {
                mvpManager.addBedBreakingPoints(player, arena);
            }
        }
    }

    @EventHandler
    public void onGameEnd(GameEndEvent e) {
        IArena arena = e.getArena();
        String group = arena.getGroup();
        if (group.equalsIgnoreCase("RankedSolo") || group.equalsIgnoreCase("RankedDuplas")
                || group.equalsIgnoreCase("Ranked1v1") || group.equalsIgnoreCase("Ranked4s")) {
            Player mvp = mvpManager.determineMVP(arena);
            List<Player> players = e.getArena().getPlayers();
            for (Player player : players) {
                if (mvp != null) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> player.sendMessage("§7O MVP dessa partida foi: §5" + mvp.getName()), 20L);

                    int mvpCount = eloManager.getPlayerData().getInt(mvp.getUniqueId() + ".mvp", 0);
                    eloManager.getPlayerData().set(mvp.getUniqueId() + ".mvp", mvpCount + 1);

                    eloManager.savePlayerData();

                } else {
                    player.sendMessage("§cO MVP dessa partida não foi definido!");
                    player.sendMessage("§cO MVP dessa partida não foi definido!");
                }
            }
            mvpManager.reset(arena);
        }
    }
}
