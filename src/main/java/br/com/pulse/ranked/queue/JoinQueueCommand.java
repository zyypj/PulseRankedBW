package br.com.pulse.ranked.queue;

import br.com.pulse.ranked.elo.EloManager;
import com.tomkeuper.bedwars.api.BedWars;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JoinQueueCommand implements CommandExecutor, Listener {

    private final QueueManager queueManager;
    private final EloManager eloManager;
    BedWars bedwarsAPI = Bukkit.getServicesManager().getRegistration(BedWars.class).getProvider();

    public JoinQueueCommand(QueueManager queueManager, EloManager eloManager) {
        this.queueManager = queueManager;
        this.eloManager = eloManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser executado por jogadores.");
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("bw.vip") && bedwarsAPI.getStatsUtil().getPlayerWins(player.getUniqueId()) < 100) {
            player.sendMessage("");
            player.sendMessage("§c§lVocê precisa ter §a§lVIP §c§l ou mais de");
            player.sendMessage("§c§l100 WINS para entrar em uma fila");
            player.sendMessage("");
            return true;
        }

        openJoinMenu(player);
        return true;
    }

    public void openJoinMenu(Player player) {
        Inventory joinMenu = Bukkit.createInventory(player, 54, "§7Entrar em uma Fila");

        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§aInformações Pessoais");
        List<String> infoLore = new ArrayList<>();
        infoLore.add("§7Veja suas informações pessoais");
        infoLore.add("");
        infoLore.add("§7Seu Rank: " + eloManager.getRank(eloManager.getElo(player.getUniqueId(), "geral")));
        infoLore.add("§7Seu Elo Geral: §5" + eloManager.getElo(player.getUniqueId(), "geral"));
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        joinMenu.setItem(13, infoItem);

        ItemStack Ranked1v1Item = new ItemStack(Material.BED);
        ItemMeta Ranked1v1Meta = Ranked1v1Item.getItemMeta();
        Ranked1v1Meta.setDisplayName("§a1v1 Ranked");
        List<String> Ranked1v1Lore = new ArrayList<>();
        Ranked1v1Lore.add("§7Entrar na fila para 1v1 Ranked");
        Ranked1v1Lore.add("");
        Ranked1v1Lore.add("§7Elo 1v1: §5" + eloManager.getElo(player.getUniqueId(), "ranked1v1"));
        Ranked1v1Lore.add("");
        Ranked1v1Lore.add("§eClique para entrar na fila");
        Ranked1v1Meta.setLore(Ranked1v1Lore);
        Ranked1v1Item.setItemMeta(Ranked1v1Meta);
        joinMenu.setItem(31, Ranked1v1Item);

        ItemStack RankedSoloItem = new ItemStack(Material.BED);
        ItemMeta RankedSoloMeta = RankedSoloItem.getItemMeta();
        RankedSoloMeta.setDisplayName("§aSolo Ranked");
        List<String> RankedSoloLore = new ArrayList<>();
        RankedSoloLore.add("§7Entrar na fila para Solo Ranked");
        RankedSoloLore.add("");
        RankedSoloLore.add("§7Elo Solo: §5" + eloManager.getElo(player.getUniqueId(), "rankedsolo"));
        RankedSoloLore.add("");
        RankedSoloLore.add("§eClique para entrar na fila");
        RankedSoloMeta.setLore(RankedSoloLore);
        RankedSoloItem.setItemMeta(RankedSoloMeta);
        joinMenu.setItem(39, RankedSoloItem);

        ItemStack RankedDuplasItem = new ItemStack(Material.BED, 2);
        ItemMeta RankedDuplasMeta = RankedDuplasItem.getItemMeta();
        RankedDuplasMeta.setDisplayName("§aDuplas Ranked");
        List<String> RankedDuplasLore = new ArrayList<>();
        RankedDuplasLore.add("§7Entrar em filas para Duplas Ranked");
        RankedDuplasLore.add("");
        RankedDuplasLore.add("§7Elo Duplas: §5" + eloManager.getElo(player.getUniqueId(), "rankedduplas"));
        RankedDuplasLore.add("");
        RankedDuplasLore.add("§eClique para entrar na fila");
        RankedSoloMeta.setLore(RankedDuplasLore);
        RankedSoloItem.setItemMeta(RankedSoloMeta);
        joinMenu.setItem(41, RankedDuplasItem);

        player.openInventory(joinMenu);
    }

    @EventHandler
    public void onClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (e.getView().getTitle().equalsIgnoreCase("§7Entrar em uma Fila")) {
            e.setCancelled(true);
            if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) {
                return;
            }
            if (e.getSlot() == 13 && e.getCurrentItem().getType() == Material.PAPER) {
                UUID playerUUID = player.getUniqueId();
                int eloSolo = eloManager.getElo(playerUUID, "rankedsolo");
                int eloDuplas = eloManager.getElo(playerUUID, "rankedduplas");
                int elo1v1 = eloManager.getElo(playerUUID, "ranked1v1");
                int elo4v4 = eloManager.getElo(playerUUID, "ranked4v4");
                int eloGeral = (eloSolo + eloDuplas + elo1v1 + elo4v4) / 4;
                String rank = eloManager.getRank(eloGeral);

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
                player.closeInventory();

            } else if (e.getSlot() == 31 && e.getCurrentItem().getType() == Material.BED) {
                if (bedwarsAPI.getPartyUtil().hasParty(player)) {
                    player.sendMessage("§cVocê não pode entrar em uma fila ranqueada em party!");
                    player.closeInventory();
                    player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 1);
                    return;
                }
                queueManager.joinQueue(player, "Ranked1v1");
                player.closeInventory();
            } else if (e.getSlot() == 39 && e.getCurrentItem().getType() == Material.BED) {
                if (bedwarsAPI.getPartyUtil().hasParty(player)) {
                    player.sendMessage("§cVocê não pode entrar em uma fila ranqueada em party!");
                    player.closeInventory();
                    player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 1);
                    return;
                }
                bedwarsAPI.getArenaUtil().joinRandomFromGroup(player, "RankedSolo");
                player.closeInventory();
            } else  if (e.getSlot() == 41 && e.getCurrentItem().getType() == Material.BED) {
                if (!bedwarsAPI.getPartyUtil().hasParty(player)) {
                    player.sendMessage("§cVocê precisa estar em uma party para entrar nessa fila!");
                    player.closeInventory();
                    player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 1);
                    return;
                }
                if (!bedwarsAPI.getPartyUtil().isOwner(player)) {
                    player.sendMessage("§cApenas o dono da party pode fazer isso!");
                    player.closeInventory();
                    player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 1);
                    return;
                }
                if (bedwarsAPI.getPartyUtil().partySize(player) != 2) {
                    player.sendMessage("§cVocê precisa ter exatamente 2 pessoas na party para entrar nessa fila!");
                    player.closeInventory();
                    player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 1);
                    return;
                }
                bedwarsAPI.getArenaUtil().joinRandomFromGroup(player, "RankedDuplas");
                player.closeInventory();
            }
        }
    }
}

