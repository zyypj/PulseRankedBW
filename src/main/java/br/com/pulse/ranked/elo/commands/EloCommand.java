package br.com.pulse.ranked.elo.commands;

import br.com.pulse.ranked.elo.EloManager;
import br.com.pulse.ranked.misc.Utility;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class EloCommand implements CommandExecutor {

    private final EloManager eloManager;

    public EloCommand(EloManager eloManager) {
        this.eloManager = eloManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        UUID playerUUID = player.getUniqueId();
        int eloSolo = eloManager.getElo(playerUUID, "rankedsolo");
        int eloDuplas = eloManager.getElo(playerUUID, "rankedduplas");
        int elo1v1 = eloManager.getElo(playerUUID, "ranked1v1");
        int elo4v4 = eloManager.getElo(playerUUID, "ranked4v4");
        int eloGeral = (eloSolo + eloDuplas + elo1v1 + elo4v4) / 4;
        String rank = eloManager.getRank(eloGeral);

        if (args.length == 0) {
            player.sendMessage("§5§lPRanked §7§lBed Wars");
            player.sendMessage("");
            player.sendMessage("§7Estatísticas de §l" + player.getName());
            player.sendMessage("");
            player.sendMessage("§7Rank: " + rank);
            player.sendMessage("§7Elo Geral: §5" + eloGeral);
            player.sendMessage("");
            player.sendMessage("§7Elo Solo: §5" + eloSolo);
            player.sendMessage("§7Elo Duplas: §5" + eloDuplas);
            player.sendMessage("§7Elo 1v1: §5" + elo1v1);
            player.sendMessage("§7Elo 4v4: §5" + elo4v4);
            player.sendMessage("");
            return true;
        }

        if (args.length == 1) {

            if (args[0].equalsIgnoreCase("solo")) {
                player.sendMessage("§7Elo Solo de §l" + player.getName() + "§7: §5" + eloSolo);
                return true;
            } else if (args[0].equalsIgnoreCase("duplas")) {
                player.sendMessage("§7Elo Duplas de §l" + player.getName() + "§7: §5" + eloDuplas);
                return true;
            } else if (args[0].equalsIgnoreCase("1v1")) {
                player.sendMessage("§7Elo 1v1 de §l" + player.getName() + "§7: §5" + elo1v1);
                return true;
            } else if (args[0].equalsIgnoreCase("4v4")) {
                player.sendMessage("§7Elo 4v4 de §l" + player.getName() + "§7: §5" + elo4v4);
                return true;
            } else if (args[0].equalsIgnoreCase("geral")) {
                player.sendMessage("§7Elo Geral de §l" + player.getName() + "§7: §5" + eloGeral);
                return true;
            } else {
                player.sendMessage("§cUse: /elo <geral/solo/duplas/1v1/4v4>");
                return true;
            }
        }

        if (args.length == 2) {

            Player target = Bukkit.getPlayer(args[0]);

            if (target == null || !target.isOnline()) {
                player.sendMessage("§cJogador não encontrado!");
                return true;
            }

            UUID targetUUID = target.getUniqueId();
            int eloSoloT = eloManager.getElo(targetUUID, "rankedsolo");
            int eloDuplasT = eloManager.getElo(targetUUID, "rankedduplas");
            int elo1v1T = eloManager.getElo(targetUUID, "ranked1v1");
            int elo4v4T = eloManager.getElo(targetUUID, "ranked4v4");
            int eloGeralT = (eloSoloT + eloDuplasT + elo1v1T + elo4v4T) / 4;

            if (args[1].equalsIgnoreCase("solo")) {
                player.sendMessage("§7Elo Solo de §l" + target.getName() + "§7: §5" + eloSoloT);
                return true;
            } else if (args[1].equalsIgnoreCase("duplas")) {
                player.sendMessage("§7Elo Duplas de §l" + target.getName() + "§7: §5" + eloDuplasT);
                return true;
            } else if (args[1].equalsIgnoreCase("1v1")) {
                player.sendMessage("§7Elo 1v1 de §l" + target.getName() + "§7: §5" + elo1v1T);
                return true;
            } else if (args[1].equalsIgnoreCase("4v4")) {
                player.sendMessage("§7Elo 4v4 de §l" + target.getName() + "§7: §5" + elo4v4T);
                return true;
            } else if (args[1].equalsIgnoreCase("geral")) {
                player.sendMessage("§7Elo geral de §l" + target.getName() + "§7: §5" + eloGeralT);
                return true;
            } else {
                player.sendMessage("§cUse: /elo <jogador> <geral/solo/duplas/1v1/4v4>");
                return true;
            }
        }

        //elo set <jogador> <geral/solo/duplas/1v1/4v4> <número>
        if (args.length == 4 && args[0].equalsIgnoreCase("set")) {

            if (!player.hasPermission("bwranked.admin")) {
                player.sendMessage("§cComando não encontrado ou você não tem permissão!");
                return true;
            }

            String number = args[3];
            int newElo;
            try {
                newElo = Integer.parseInt(number);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cUse: /elo set <jogador> <geral/solo/duplas/1v1/4v4> <NÚMERO>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);

            if (target == null || !target.isOnline()) {
                player.sendMessage("§cJogador não encontrado");
                return true;
            }

            UUID targetUUID = target.getUniqueId();

            if (args[2].equalsIgnoreCase("solo")) {
                eloManager.setElo(targetUUID, "rankedsolo", newElo);
                sender.sendMessage("§7O Elo Solo de §l" + target.getName() + " §7foi definido para §5" + newElo);
                return true;
            } else if (args[2].equalsIgnoreCase("duplas")) {
                eloManager.setElo(targetUUID, "rankedduplas", newElo);
                sender.sendMessage("§7O Elo Duplas de §l" + target.getName() + " §7foi definido para §5" + newElo);
                return true;
            } else if (args[2].equalsIgnoreCase("1v1")) {
                eloManager.setElo(targetUUID, "ranked1v1", newElo);
                sender.sendMessage("§7O Elo 1v1 de §l" + target.getName() + " §7foi definido para §5" + newElo);
                return true;
            } else {
                sender.sendMessage("§cUse: /elo set <1v1/solo/duplas> <número>");
                return true;
            }

        }

        player.sendMessage("§cUse: /elo <geral/solo/duplas/1v1/4v4> ou");
        player.sendMessage("§cUse: /elo <jogador> <geral/solo/duplas/1v1/4v4>");
        return true;
    }
}

