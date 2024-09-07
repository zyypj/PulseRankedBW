package br.com.pulse.ranked.misc.fourS.match;

import br.com.pulse.ranked.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public class MatchCommand implements CommandExecutor {

    private final MatchStats matchStats;

    public MatchCommand(Main plugin) {
        this.matchStats = new MatchStats(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando só pode ser usado por jogadores.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("bw.helper")) {
            player.sendMessage("§cComando não encontrado ou você não tem permissão!");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage("Uso correto: /partida <id>");
            return true;
        }

        String matchIdN = args[0];
        String matchId = "bw4s-" + matchIdN;

        // Carregar os dados da partida
        matchStats.reloadConfig();
        FileConfiguration config = matchStats.getConfig(); // Adicione um método para acessar a configuração
        System.out.println(config);
        if (config.contains(matchId)) {
            String map = config.getString(matchId + ".Mapa");
            String group = config.getString(matchId + ".Modo");
            String date = config.getString(matchId + ".Data");
            List<String> team1 = config.getStringList(matchId + ".Time1");
            List<String> team2 = config.getStringList(matchId + ".Time2");
            List<String> topKills = config.getStringList(matchId + ".TopKillsFinais");
            List<String> topBedBreaking = config.getStringList(matchId + ".TopBedBreaking");

            // Enviar mensagens ao jogador
            player.sendMessage("");
            player.sendMessage("§7§lInformações da Partida §5§l" + matchId);
            player.sendMessage("§7Mapa: §5" + map);
            player.sendMessage("§7Modo: §c" + group);
            player.sendMessage("");
            player.sendMessage("§7Data: §5" + date);

            player.sendMessage("");
            player.sendMessage("§9Time Azul:");
            for (String member : team1) {
                player.sendMessage("§7- §9" + member);
            }

            player.sendMessage("");
            player.sendMessage("§cTime Vermelho:");
            for (String member : team2) {
                player.sendMessage("§7- §c" + member);
            }

            player.sendMessage("");
            player.sendMessage("§7§lTop Kills Finais:");
            for (String kill : topKills) {
                player.sendMessage("§7- §5" + kill);
            }

            player.sendMessage("");
            player.sendMessage("§7§lTop Destruição de Camas:");
            for (String bedBreak : topBedBreaking) {
                player.sendMessage("§7- §6" + bedBreak);
            }

        } else {
            player.sendMessage("§cID de partida não encontrado.");
        }

        return true;
    }
}
