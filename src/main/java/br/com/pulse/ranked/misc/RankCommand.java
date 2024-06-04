package br.com.pulse.ranked.misc;

import br.com.pulse.ranked.elo.EloManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class RankCommand implements CommandExecutor {

    private final EloManager eloManager;

    public RankCommand(EloManager eloManager) {
        this.eloManager = eloManager;
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
            int elo4v4 = eloManager.getElo(playerUUID, "ranked4v4");
            int eloSoma = (eloSolo + elo1v1 + elo4v4) / 3;
            player.sendMessage("§7Seu Rank é: §5" + eloManager.getRank(eloSoma));
            return true;
        }
        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            UUID targetUUID = target.getUniqueId();
            int eloSolo = eloManager.getElo(targetUUID, "rankedsolo");
            int elo1v1 = eloManager.getElo(targetUUID, "ranked1v1");
            int elo4v4 = eloManager.getElo(targetUUID, "ranked4v4");
            int eloSoma = (eloSolo + elo1v1 + elo4v4) / 3;
            player.sendMessage("§7O Rank de §l" + target.getName() + " §7: §5" + eloManager.getRank(eloSoma));
            return true;
        }
        player.sendMessage("§cUse /rank <nome>.");
        return true;
    }
}

