package br.com.pulse.ranked.misc.listeners;

import br.com.pulse.ranked.Main;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.kasp.rbw.api.RankedBedwarsAPI;
import com.leafplugins.plugins.cache.objects.User;
import com.leafplugins.punish.platform.commons.api.LeafPunishAPI;
import com.leafplugins.punish.platform.commons.api.objects.Punishment;
import com.leafplugins.punish.platform.commons.api.objects.PunishmentType;
import com.leafplugins.punish.platform.commons.api.objects.PunishmentTypes;
import com.tomkeuper.bedwars.api.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.events.player.PlayerJoinArenaEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerLeaveArenaEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.UUID;

public class ListenersMisc implements Listener {
	
	private final Main plugin;
	private final RankedBedwarsAPI rankedBedwarsAPI;
	BedWars bwAPI = Bukkit.getServicesManager().getRegistration(BedWars.class).getProvider();
	LeafPunishAPI lpAPI = LeafPunishAPI.getApi();
	
	public ListenersMisc(Main plugin) {
		this.plugin = plugin;
		this.rankedBedwarsAPI = plugin.getServer().getServicesManager().getRegistration(RankedBedwarsAPI.class).getProvider();
	}
	
	@EventHandler
	public void onPlayerArenaJoin(PlayerJoinArenaEvent e) {
		IArena arena = e.getArena();
		String group = arena.getGroup();
		Player player = e.getPlayer();
		if (group.equalsIgnoreCase("Ranked2v2CM")) {
			int playersInArena = arena.getPlayers().size();
			if (playersInArena == 4) {
				e.setCancelled(true);
			}
		}
		if (group.equalsIgnoreCase("Ranked4s")) {
			int playersInArena = arena.getPlayers().size();
			if (playersInArena == 8) {
				e.setCancelled(true);
				
				com.kasp.rbw.instance.Player rbwPlayer = this.rankedBedwarsAPI.getPlayerByName(player.getName());
				if (rbwPlayer != null) {
					this.rankedBedwarsAPI.finishGameOf(rbwPlayer, 5);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerArenaQuit(PlayerLeaveArenaEvent e) {
		IArena arena = e.getArena();
		String group = arena.getGroup();
		Player player = e.getPlayer();
		if (group.startsWith("Ranked")) {
			if (!player.hasPermission("bw.staff")) {
				if (player.isOnline()) {
					player.sendMessage("§c§lVocê saiu no meio de uma partida ranqueada.");
					player.sendMessage("§cEntre nela novamente para evitar uma punição!");
				}
				Bukkit.getScheduler().runTaskLater(plugin, () -> {
					if (!bwAPI.getArenaUtil().isPlaying(player)) {
						if (!lpAPI.hasActivePunishment(player.getUniqueId(), PunishmentTypes.BAN)) {
							banPlayer(player.getUniqueId());
						}
					}
					IArena arena2 = bwAPI.getArenaUtil().getArenaByPlayer(player);
					if (!arena.equals(arena2)) {
						if (!lpAPI.hasActivePunishment(player.getUniqueId(), PunishmentTypes.BAN)) {
							banPlayer(player.getUniqueId());
						}
					}
				}, 6000L);
			}
		}
	}
	
	private void banPlayer(UUID uuid) {
		PunishmentType punishmentType = lpAPI.getPunishmentType("TEMPBAN"); // Certifique-se que "BAN" é um tipo válido
		
		long currentTime = System.currentTimeMillis();
		long oneDayInMillis = 24 * 60 * 60 * 1000; // 1 dia em milissegundos
		String endDate = String.valueOf(currentTime + oneDayInMillis);
		
		Punishment punishment = new Punishment() {
			@Override
			public PunishmentType getType() {
				return punishmentType;
			}
			
			@Override
			public UUID getPlayer() {
				return uuid;
			}
			
			@Override
			public UUID getAuthor() {
				return UUID.fromString("00000000-0000-0000-0000-000000000000");
			}
			
			@Override
			public User getPlayerUser() {
				return null;
			}
			
			@Override
			public User getAuthorUser() {
				return null;
			}
			
			@Override
			public boolean isAuthorConsole() {
				return true;
			}
			
			@Override
			public String getId() {
				return null;
			}
			
			@Override
			public String getReason() {
				return "Ranked Match Quit";
			}
			
			@Override
			public String getServerName() {
				return "global";
			}
			
			@Override
			public String getProof() {
				return "";
			}
			
			@Override
			public String getCreateDate() {
				return String.valueOf(currentTime);
			}
			
			@Override
			public String getEndDate() {
				return endDate;
			}
			
			@Override
			public String getDisconnectMessage() {
				return "You have been banned for leaving a ranked match.";
			}
			
			@Override
			public List<String> getWarnMessage() {
				return List.of("Warning: Do not leave ranked matches.");
			}
			
			@Override
			public List<String> getBroadcast() {
				return List.of();
			}
			
			@Override
			public String applyPlaceholders(String s) {
				return "";
			}
			
			@Override
			public boolean isActive() {
				return true;
			}
			
			@Override
			public boolean isSilent() {
				return false;
			}
			
			@Override
			public boolean isExpired() {
				return false;
			}
			
			@Override
			public boolean isServerSync() {
				return true;
			}
			
			@Override
			public boolean isGlobal() {
				return true;
			}
			
			@Override
			public boolean isBan() {
				return true;
			}
			
			@Override
			public boolean isMute() {
				return false;
			}
			
			@Override
			public boolean isWarn() {
				return false;
			}
			
			@Override
			public boolean isIP() {
				return false;
			}
			
			@Override
			public JsonObject asJson() {
				Gson gson = new Gson();
				return gson.fromJson(gson.toJson(this, JsonObject.class), JsonObject.class);
			}
		};
		lpAPI.registerPunishment(punishment);
	}
}
