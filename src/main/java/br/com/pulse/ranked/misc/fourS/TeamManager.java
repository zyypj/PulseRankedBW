package br.com.pulse.ranked.misc.fourS;

import br.com.pulse.ranked.Main;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.arena.team.ITeamAssigner;
import com.tomkeuper.bedwars.api.events.gameplay.TeamAssignEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerJoinArenaEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.LinkedList;
import java.util.List;

public class TeamManager implements Listener, ITeamAssigner {

    private final LinkedList<ITeam> teams = new LinkedList<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinArenaEvent e) {
        List<Player> players = e.getArena().getPlayers();
        IArena arena = e.getArena();
        if (arena.getGroup().equalsIgnoreCase("Ranked4s")) {
            if (players.size() != 8) {
                arena.setStatus(GameState.waiting);
                return;
            }

            arena.setStatus(GameState.starting);
        }
    }

    @EventHandler
    public void teamAssign(TeamAssignEvent e) {
        IArena arena = e.getArena();

        if (arena.getGroup().equalsIgnoreCase("Ranked2v2CM")) {

            List<Player> players = arena.getPlayers();

            if (players.size() != 4) {
                Main.debug("Detectado != 4");
                for (Player player : players) {
                    arena.removePlayer(player, true);
                    player.sendMessage("");
                    player.sendMessage("§cOcorreu um erro na criação da partida!");
                    player.sendMessage("§cPedimos perdão pelo ocorrido.");
                    player.sendMessage("");
                }
                return;
            }

            ITeam timeV = arena.getTeam("Vermelho");
            ITeam timeC = arena.getTeam("Ciano");

            List<Player> player12 = players.subList(0, 2);
            List<Player> player34 = players.subList(2, 4);

            for (Player player : player12) {
                timeV.addPlayers(player);
            }

            for (Player player : player34) {
                timeC.addPlayers(player);
            }

            return;
        }

        if (arena.getGroup().equalsIgnoreCase("Ranked4s")) {

            List<Player> players = arena.getPlayers();

            ITeam timeV = arena.getTeam("Vermelho");
            ITeam timeA = arena.getTeam("Azul");

            List<Player> player1234 = players.subList(0, 4);
            List<Player> player5678 = players.subList(4, 8);

            for (Player player : player1234) {
                timeV.addPlayers(player);
            }

            for (Player player : player5678) {
                timeA.addPlayers(player);
            }

            ITeam timeVe = arena.getTeam("Verde");
            if (!timeVe.getMembers().isEmpty()) {
                for (Player player : timeVe.getMembers()) {
                    arena.removePlayer(player, false);
                }
            }

            ITeam timeAm = arena.getTeam("Amarelo");
            if (!timeAm.getMembers().isEmpty()) {
                for (Player player : timeAm.getMembers()) {
                    arena.removePlayer(player, false);
                }
            }
        }
    }

    @Override
    public void assignTeams(IArena arena) {
        teams.addAll(arena.getTeams());
    }
}
