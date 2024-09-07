package br.com.pulse.ranked.misc;

import br.com.pulse.ranked.elo.EloManager;
import com.kasp.rbw.api.RankedBedwarsAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class RankCommand implements CommandExecutor {
	
	private final EloManager eloManager;
	private final Map<UUID, Boolean> displayPreferences;
	
	RankedBedwarsAPI api = Bukkit.getServicesManager().getRegistration(RankedBedwarsAPI.class).getProvider();
	
	public RankCommand(EloManager eloManager, Map<UUID, Boolean> displayPreferences) {
		this.eloManager = eloManager;
		this.displayPreferences = displayPreferences;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Apenas jogadores!");
			return true;
		}
		Player player = (Player) sender;
		if (args.length == 0) {
			UUID playerUUID = player.getUniqueId();
			int eloSolo = eloManager.getElo(playerUUID, "rankedsolo");
			int elo1v1 = eloManager.getElo(playerUUID, "ranked1v1");
			int elo4v4 = api.getElo(player);
			int elo2v2cm = eloManager.getElo(playerUUID, "ranked2v2cm");
			int eloDuplas = eloManager.getElo(playerUUID, "rankedduplas");
			int eloSoma = (elo1v1 + elo2v2cm + elo4v4) / 3;
			player.sendMessage("§7Seu Rank é: §5" + eloManager.getRank(eloSoma));
			return true;
		}
		if (args.length == 1) {
			Player target = Bukkit.getPlayer(args[0]);
			UUID targetUUID = target.getUniqueId();
			
			boolean displayTag = displayPreferences.getOrDefault(target.getUniqueId(), true);
			
			if (!displayTag) {
				player.sendMessage("§cEsse jogador está com a visibilidade de rank desativada!");
				return true;
			}
			
			int eloSolo = eloManager.getElo(targetUUID, "rankedsolo");
			int elo1v1 = eloManager.getElo(targetUUID, "ranked1v1");
			int elo4v4 = api.getElo(target);
			int elo2v2cm = eloManager.getElo(targetUUID, "ranked2v2cm");
			int eloDuplas = eloManager.getElo(targetUUID, "rankedduplas");
			int eloSoma = (elo1v1 + elo2v2cm + elo4v4) / 3;
			player.sendMessage("§7O Rank de §l" + target.getName() + " §7: §5" + eloManager.getRank(eloSoma));
			return true;
		}
		player.sendMessage("§cUse /rank <nome>");
		return true;
	}
}

