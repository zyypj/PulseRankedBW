package br.com.pulse.ranked.misc.fourS;

import br.com.pulse.ranked.Main;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.generator.IGenerator;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerBedBreakEvent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class ForgeManager implements Listener {

    private final Main plugin;

    public ForgeManager(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void gameStatusChange(GameStateChangeEvent e) {
        if (e.getArena().getGroup().equalsIgnoreCase("Ranked4s")) {
            GameState gameState = e.getNewState();

            if (gameState.name().equalsIgnoreCase("playing")) {
                IArena arena = e.getArena();
                ITeam verde = arena.getTeam("Verde");

                List<IGenerator> generators = verde.getGenerators();
                for (IGenerator generator : generators) {
                    generator.disable();
                }
            }
        }
    }

    @EventHandler
    public void bedBreak(PlayerBedBreakEvent e) {
        IArena arena = e.getArena();
        ITeam verde = arena.getTeam("Verde");

        if (arena.getGroup().equalsIgnoreCase("Ranked4s")) {
            if (e.getVictimTeam().getName().equalsIgnoreCase("Azul") ||
                    e.getVictimTeam().getName().equalsIgnoreCase("Vermelho")) {
                List<IGenerator> generators = verde.getGenerators();
                for (IGenerator generator : generators) {
                    generator.enable();
                }

                List<Player> players = arena.getPlayers();
                for (Player player : players) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        player.sendMessage("");
                        player.sendMessage("§7Ilha §a§lVERDE §7ativada!");
                        player.sendMessage("");
                        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
                    }, 40L);
                }
            }
        }
    }
}
