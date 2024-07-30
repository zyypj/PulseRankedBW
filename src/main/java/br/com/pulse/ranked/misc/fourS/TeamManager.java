package br.com.pulse.ranked.misc.fourS;

import br.com.pulse.ranked.Main;
import com.tomkeuper.bedwars.api.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.arena.team.ITeamAssigner;
import com.tomkeuper.bedwars.api.events.server.ArenaEnableEvent;
import me.agent.teamselector.api.events.TeamSelectorOpenEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.LinkedList;
import java.util.List;

public class TeamManager implements Listener, ITeamAssigner {

    private final LinkedList<ITeam> teams = new LinkedList<>();

    BedWars bedwarsAPI = Bukkit.getServicesManager().getRegistration(BedWars.class).getProvider();

    @EventHandler
    public void onArenaLoad(ArenaEnableEvent event) {
        if (event.getArena().getGroup().equalsIgnoreCase("Ranked2v2CM")) {
            event.getArena().setTeamAssigner(this);
        }
    }

    @Override
    public void assignTeams(IArena arena) {
        teams.addAll(arena.getTeams());

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
        }
        teams.clear();
    }

    @EventHandler
    public void tmOnOpen(TeamSelectorOpenEvent e) {

        Player player = e.getPlayer();

        IArena arena = bedwarsAPI.getArenaUtil().getArenaByPlayer(player);

        if (arena != null) {
            if (!player.hasPermission("bw.admin")) {
                if (arena.getGroup().equalsIgnoreCase("Ranked4s")
                        || arena.getGroup().equalsIgnoreCase("Ranked2v2CM")
                        || arena.getGroup().equalsIgnoreCase("CxC4v4")) {
                    e.setCancelled(true);
                    player.sendMessage("§cFuncionalidade desativada no modo " + arena.getGroup());
                }
            }
        }
    }
}
