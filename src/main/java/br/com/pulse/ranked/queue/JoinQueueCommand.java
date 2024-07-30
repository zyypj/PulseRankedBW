package br.com.pulse.ranked.queue;

import br.com.pulse.ranked.elo.EloManager;
import com.github.syncwrld.prankedbw.bw4sbot.api.Ranked4SApi;
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
    Ranked4SApi api = Bukkit.getServicesManager().getRegistration(Ranked4SApi.class).getProvider();

    public JoinQueueCommand(QueueManager queueManager, EloManager eloManager) {
        this.queueManager = queueManager;
        this.eloManager = eloManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cEste comando só pode ser executado por jogadores.");
            return true;
        }
        if (!player.hasPermission("bw.vip") && bedwarsAPI.getStatsUtil().getPlayerWins(player.getUniqueId()) < 50) {
            player.sendMessage("");
            player.sendMessage("§c§lVocê precisa ter §a§lVIP §c§l ou mais de");
            player.sendMessage("§c§l50 WINS para entrar em uma fila");
            player.sendMessage("");
            return true;
        }
        openJoinMenu(player);
        return true;
    }

    public void openJoinMenu(Player player) {
        Inventory joinMenu = Bukkit.createInventory(player, 45, "§7Entrar em uma Fila");

        UUID playerUUID = player.getUniqueId();

        int elo1v1 = eloManager.getElo(playerUUID, "ranked1v1");
        int elo4v4 = api.getElo(player);
        int elo2v2 = eloManager.getElo(playerUUID, "ranked2v2cm");
        int eloGeral = (elo1v1 + elo4v4 + elo2v2) / 3;
        String rank = eloManager.getRank(eloGeral);

        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§aInformações Pessoais");
        List<String> infoLore = new ArrayList<>();
        infoLore.add("§7Veja suas informações pessoais");
        infoLore.add("");
        infoLore.add("§7Seu Rank: " + rank);
        infoLore.add("§7Seu Elo Geral: §5" + eloGeral);
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        joinMenu.setItem(13, infoItem);

        ItemStack Ranked1v1Item = new ItemStack(Material.BED);
        ItemMeta Ranked1v1Meta = Ranked1v1Item.getItemMeta();
        Ranked1v1Meta.setDisplayName("§aRanked 1v1");
        List<String> Ranked1v1Lore = new ArrayList<>();
        Ranked1v1Lore.add("§7Entrar na fila para 1v1 Ranked");
        Ranked1v1Lore.add("");
        Ranked1v1Lore.add("§7Elo 1v1: §5" + eloManager.getElo(player.getUniqueId(), "ranked1v1"));
        Ranked1v1Lore.add("");
        Ranked1v1Lore.add("§eClique para entrar na fila");
        Ranked1v1Meta.setLore(Ranked1v1Lore);
        Ranked1v1Item.setItemMeta(Ranked1v1Meta);
        joinMenu.setItem(30, Ranked1v1Item);

        ItemStack Ranked2v2CMItem = new ItemStack(Material.BED, 2);
        ItemMeta Ranked2v2CMMeta = Ranked2v2CMItem.getItemMeta();
        Ranked2v2CMMeta.setDisplayName("§aRanked 2v2");
        List<String> Ranked2v2CMLore = new ArrayList<>();
        Ranked2v2CMLore.add("§8Cross-Map");
        Ranked2v2CMLore.add("");
        Ranked2v2CMLore.add("§7Entrar na fila para Ranked 2v2");
        Ranked2v2CMLore.add("");
        Ranked2v2CMLore.add("§7Elo 2v2: §5" + eloManager.getElo(player.getUniqueId(), "ranked2v2cm"));
        Ranked2v2CMLore.add("");
        Ranked2v2CMLore.add("§eClique para entrar na fila");
        Ranked2v2CMMeta.setLore(Ranked2v2CMLore);
        Ranked2v2CMItem.setItemMeta(Ranked2v2CMMeta);
        joinMenu.setItem(32, Ranked2v2CMItem);

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
            ItemStack clickedItem = e.getCurrentItem();
            if (clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) {
                String displayName = clickedItem.getItemMeta().getDisplayName();
                if (e.getSlot() == 13 && clickedItem.getType() == Material.PAPER) {
                    UUID playerUUID = player.getUniqueId();
                    int eloSolo = eloManager.getElo(playerUUID, "rankedsolo");
                    int eloDuplas = eloManager.getElo(playerUUID, "rankedduplas");
                    int elo1v1 = eloManager.getElo(playerUUID, "ranked1v1");
                    int elo4v4 = api.getElo(player);
                    int elo2v2 = eloManager.getElo(playerUUID, "ranked2v2cm");
                    int eloGeral = (elo1v1 + elo4v4 + elo2v2) / 3;
                    String rank = eloManager.getRank(eloGeral);

                    player.sendMessage("§5§lPRanked §7§lBed Wars");
                    player.sendMessage("");
                    player.sendMessage("§7Estatísticas de §l" + player.getName());
                    player.sendMessage("");
                    player.sendMessage("§7Rank: " + rank);
                    player.sendMessage("§7Elo Geral: §5" + eloGeral);
                    player.sendMessage("");
                    player.sendMessage("§7Elo 1v1: §5" + elo1v1);
                    player.sendMessage("§7Elo 2v2: §5" + elo2v2);
                    player.sendMessage("§7Elo 4v4: §5" + elo4v4);
                    player.sendMessage("");
                    player.closeInventory();

                } else if (e.getSlot() == 30 && clickedItem.getType() == Material.BED && displayName.equals("§aRanked 1v1")) {
                    if (bedwarsAPI.getPartyUtil().hasParty(player)) {
                        player.sendMessage("§cVocê não pode entrar em uma fila ranqueada em party!");
                        player.closeInventory();
                        player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 1);
                        return;
                    }
                    queueManager.joinQueue(player, "Ranked1v1");
                    player.closeInventory();
                } else if (e.getSlot() == 32 && clickedItem.getType() == Material.BED && displayName.equals("§aRanked 2v2")) {
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
                    queueManager.joinQueue(player, "Ranked2v2CM");
                    player.closeInventory();
                } else if (e.getSlot() == 41 && clickedItem.getType() == Material.BED && displayName.equals("§aRanked Duplas")) {
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
}

