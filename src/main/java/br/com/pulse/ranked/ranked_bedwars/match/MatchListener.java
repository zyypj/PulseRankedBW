package br.com.pulse.ranked.ranked_bedwars.match;

import br.com.pulse.ranked.Main;
import br.com.pulse.ranked.mvp.MVPManager;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.events.gameplay.GameEndEvent;
import com.tomkeuper.bedwars.api.events.gameplay.GameStateChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;

public class MatchListener implements Listener {

	private final MatchStats matchStats;
	private final MVPManager mvpManager;
	private List<String> team1;
	private List<String> team2;
	private String id;

	public MatchListener() {
		this.matchStats = new MatchStats(Main.getInstance());
		this.mvpManager = MVPManager.getMvpManager();
	}

	@EventHandler
	public void gameStart(GameStateChangeEvent e) {
		IArena arena = e.getArena();

		if (e.getNewState().equals(GameState.playing)) {
			if (arena.getGroup().startsWith("Ranked")) {
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
	public void gameEnd(GameEndEvent e) {
		IArena arena = e.getArena();
		String map = arena.getDisplayName();
		String group = arena.getGroup();

		Map<String, Integer> playerKills = new HashMap<>();
		if (group.startsWith("Ranked")) {

			switch (group) {
				case "Ranked1v1":
					id = "bw1v1";
					break;
				case "Ranked2v2CM":
					id = "bw2v2";
					break;
				case "Ranked4s":
					id = "bw4s";
					break;
				default:
					id = "bw";
			}

			int previousMatch = Main.getInstance().getConfig().getInt("currently-match-" + id);
			int currentlyMatch = previousMatch + 1;

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
			Player mvp = mvpManager.determineMVP(arena);

			// Log para verificar os dados que estão sendo salvos
			System.out.println("Map: " + map);
			System.out.println("Team 1: " + team1);
			System.out.println("Team 2: " + team2);
			System.out.println("Mvp: " + mvp);
			System.out.println("Top Kills Finais: " + topKills);
			System.out.println("Top Bed Breaking: " + topBedBreaking);

			matchStats.saveMatch(id + "-" + currentlyMatch, map, group, team1, team2, mvp, topKills, topBedBreaking);
		}
	}
}