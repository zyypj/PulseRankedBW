package br.com.pulse.ranked.misc.mvp;

import br.com.pulse.ranked.elo.EloManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MVPCommand implements CommandExecutor {

    private final EloManager eloManager;

    public MVPCommand(EloManager eloManager) {
        this.eloManager = eloManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        if (args.length == 0) {
            int mvp = eloManager.getMvp(player);

            if (mvp == 0) {
                player.sendMessage("§cVocê nunca foi MVP de uma partida!");
                return true;
            }

            player.sendMessage("§7Você ja foi §lMVP §7de uma partida §5" + mvp + "§7 vezes.");
            return true;
        }

        if (args.length == 1) {
            String target = args[0];

            Player targetPlayer = Bukkit.getPlayer(target);

            if (targetPlayer == null) {
                player.sendMessage("§cJogador não encontrado!");
                return true;
            }

            int mvp = eloManager.getMvp(targetPlayer);
            player.sendMessage("§7O jogador " + targetPlayer.getName() + " §7foi §lMVP §7de uma partida §5" + mvp + "§7 vezes.");
            return true;
        }

        player.sendMessage("§cUse: /mvp <jogador>");
        return true;
    }
}
