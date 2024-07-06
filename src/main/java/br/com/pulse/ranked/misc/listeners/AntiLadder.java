package br.com.pulse.ranked.misc.listeners;

import com.tomkeuper.bedwars.api.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class AntiLadder implements Listener {

    BedWars bedwarsAPI = Bukkit.getServicesManager().getRegistration(BedWars.class).getProvider();

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();

        // Verifica se o bloco colocado é uma escada
        if (blockType == Material.LADDER) {
            Location blockLocation = event.getBlock().getLocation();
            IArena arena = bedwarsAPI.getArenaUtil().getArenaByPlayer(player);
            String group = arena.getGroup();
            if (group.equalsIgnoreCase("RankedSolo") || group.equalsIgnoreCase("RankedDuplas") ||
                    group.equalsIgnoreCase("Ranked1v1") || group.equalsIgnoreCase("Ranked4s")
                    || group.equalsIgnoreCase("Ranked2v2CM")) {
                for (ITeam team : arena.getTeams()) {
                    Location bedLocation = team.getBed();
                    if (bedLocation != null && isNearBed(blockLocation, bedLocation, 8)) {
                        player.sendMessage("§cVocê não pode colocar escadas perto da cama!");
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    private boolean isNearBed(Location blockLocation, Location bedLocation, int radius) {
        int xDiff = Math.abs(blockLocation.getBlockX() - bedLocation.getBlockX());
        int yDiff = Math.abs(blockLocation.getBlockY() - bedLocation.getBlockY());
        int zDiff = Math.abs(blockLocation.getBlockZ() - bedLocation.getBlockZ());

        return xDiff <= radius && yDiff <= radius && zDiff <= radius;
    }
}
