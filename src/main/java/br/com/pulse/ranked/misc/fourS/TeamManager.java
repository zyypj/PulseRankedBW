package br.com.pulse.ranked.misc.fourS;

import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.events.gameplay.TeamAssignEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerJoinArenaEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class TeamManager implements Listener {

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
