package br.com.pulse.ranked.misc.fourS.match;

import br.com.pulse.ranked.Main;
import com.github.syncwrld.prankedbw.bw4sbot.api.event.game.MatchStartEvent;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.events.gameplay.GameEndEvent;
import com.tomkeuper.bedwars.api.events.gameplay.GameStateChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchListener implements Listener {

    private List<String> team1;
    private List<String> team2;

    private String id;

    private final MatchStats matchStats;

    public MatchListener(Main plugin) {
        this.matchStats = new MatchStats(plugin);
    }

    @EventHandler
    public void gameStart(GameStateChangeEvent e) {
        IArena arena = e.getArena();

        if (e.getNewState().equals(GameState.playing)) {
            if (arena.getGroup().equalsIgnoreCase("Ranked4s")) {
                team1 = new ArrayList<>();
                team2 = new ArrayList<>();

                ITeam time1 = arena.getTeam("Azul");
                ITeam time2 = arena.getTeam("Vermelho");

                if (time1 != null && time2 != null) {
                    List<Player> players1 = time1.getMembers();
                    List<Player> players2 = time2.getMembers();

                    for (Player player : players1) {
                        team1.add(player.getName());
                    }

                    for (Player player : players2) {
                        team2.add(player.getName());
                    }

                    // Log para verificar se os times foram coletados corretamente
                    System.out.println("Time 1: " + team1);
                    System.out.println("Time 2: " + team2);
                } else {
                    // Log para verificar se os times não foram encontrados
                    if (time1 == null) {
                        System.out.println("Time Azul não encontrado!");
                    }
                    if (time2 == null) {
                        System.out.println("Time Vermelho não encontrado!");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onGameStart(MatchStartEvent e) {

        id = e.getMatch().getId();
    }

    @EventHandler
    public void gameEnd(GameEndEvent e) {
        IArena arena = e.getArena();
        String map = arena.getDisplayName();

        if (arena.getGroup().equalsIgnoreCase("Ranked4s")) {
            if (id != null) {
                Map<String, Integer> playerKills = new HashMap<>();
                for (Player player : arena.getPlayers()) {
                    int kills = arena.getPlayerKills(player, true); // true para kills finais
                    if (kills > 0) {
                        playerKills.put(player.getName(), kills);
                    }
                }

                Map<String, Integer> playerBedsDestroyed = new HashMap<>();
                for (Player player : arena.getPlayers()) {
                    int bedsDestroyed = arena.getPlayerBedsDestroyed(player);
                    if (bedsDestroyed > 0) {
                        playerBedsDestroyed.put(player.getName(), bedsDestroyed);
                    }
                }

                List<String> topKills = matchStats.getTopKills(playerKills);
                List<String> topBedBreaking = matchStats.getTopBedBreaking(playerBedsDestroyed);

                // Log para verificar os dados que estão sendo salvos
                System.out.println("id: " + id);
                System.out.println("Mapa: " + map);
                System.out.println("Team 1: " + team1);
                System.out.println("Team 2: " + team2);
                System.out.println("Top Kills Finais: " + topKills);
                System.out.println("Top Bed Breaking: " + topBedBreaking);

                matchStats.saveMatch(id, map, team1, team2, topKills, topBedBreaking);

            }
        }
    }
}