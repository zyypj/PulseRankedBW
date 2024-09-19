package br.com.pulse.ranked.elo;

import com.kasp.rbw.api.RankedBedwarsAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EloManager {

	private final FileConfiguration playerData;
	private final ConcurrentHashMap<UUID, Boolean> displayPreferences;
	private final ConcurrentHashMap<UUID, Integer> eloCache;
	private static EloManager eloManager;

	private final RankedBedwarsAPI api;

	public EloManager(FileConfiguration playerData) {
		this.playerData = playerData;
		this.displayPreferences = new ConcurrentHashMap<>();
		this.eloCache = new ConcurrentHashMap<>();
		this.api = Bukkit.getServicesManager().getRegistration(RankedBedwarsAPI.class).getProvider();
		eloManager = this;
		loadDisplayPreferences();
	}

	public int getElo(UUID playerUUID, String type) {
		// Verifica o cache primeiro
		if (eloCache.containsKey(playerUUID)) {
			return eloCache.get(playerUUID);
		}

		// Cálculo de elo geral
		if (type.equalsIgnoreCase("geral")) {
			Player player = Bukkit.getPlayer(playerUUID);
			if (player == null) return 0;

			int elo1v1 = getElo(playerUUID, "ranked1v1");
			int elo4v4 = api.getElo(player);
			int elo2v2cm = getElo(playerUUID, "ranked2v2cm");
			int elo = (elo1v1 + elo4v4 + elo2v2cm) / 3;

			eloCache.put(playerUUID, elo);  // Atualiza o cache
			return elo;
		}

		// Retorna o elo específico
		return playerData.getInt(playerUUID.toString() + "." + type, 0);
	}

	public void setElo(UUID playerUUID, String type, int elo) {
		playerData.set(playerUUID.toString() + "." + type, elo);
		eloCache.put(playerUUID, elo);  // Atualiza o cache imediatamente
		savePlayerData();
	}

	public void addElo(UUID playerUUID, int eloChange, String type) {
		int currentElo = getElo(playerUUID, type);
		int newElo = currentElo + eloChange;

		if (newElo < 0) {
			newElo = 0; // Elo não pode ser negativo
		}

		String currentRank = getRank(currentElo);
		String newRank = getRank(newElo);

		setElo(playerUUID, type, newElo);

		// Verifica se o rank mudou e envia uma mensagem ao jogador
		Player player = Bukkit.getPlayer(playerUUID);
		if (player != null && player.isOnline() && !currentRank.equals(newRank)) {
			player.sendMessage("§aParabéns! Você subiu para " + newRank + "!");
		}
	}

	public int getMvp(Player player) {
		return playerData.getInt(player.getUniqueId().toString() + ".mvp", 0);
	}

	public String getRank(int elo) {
		if (elo <= 50) return "§4[Bronze III]";
		if (elo < 100) return "§4[Bronze II]";
		if (elo < 150) return "§4[Bronze I]";
		if (elo < 250) return "§8[Prata III]";
		if (elo < 350) return "§8[Prata II]";
		if (elo < 450) return "§8[Prata I]";
		if (elo < 600) return "§6[Ouro III]";
		if (elo < 700) return "§6[Ouro II]";
		if (elo < 800) return "§6[Ouro I]";
		if (elo < 900) return "§b[Diamante III]";
		if (elo < 1100) return "§b[Diamante II]";
		return "§b[Diamante I]";
	}

	public void savePlayerData() {
		Bukkit.getScheduler().runTaskAsynchronously(Bukkit.getPluginManager().getPlugin("BedWars2023"), () -> {
			File dataFolder = new File(Bukkit.getPluginManager().getPlugin("BedWars2023").getDataFolder(), "Addons/Ranked");
			if (!dataFolder.exists()) {
				dataFolder.mkdirs();
			}
			File playerDataFile = new File(dataFolder, "playersElo.yml");
			try {
				YamlConfiguration config = new YamlConfiguration();
				for (String uuidString : playerData.getKeys(false)) {
					int eloSolo = playerData.getInt(uuidString + ".rankedsolo", 0);
					int eloDuplas = playerData.getInt(uuidString + ".rankedduplas", 0);
					int elo1v1 = playerData.getInt(uuidString + ".ranked1v1", 0);
					int elo4v4 = playerData.getInt(uuidString + ".ranked4s", 0);
					int elo2v2CM = playerData.getInt(uuidString + ".ranked2v2cm", 0);
					int eloGeral = playerData.getInt(uuidString + ".rankedgeral", 0);
					int mvpCount = playerData.getInt(uuidString + ".mvp", 0);

					config.set(uuidString + ".rankedgeral", eloGeral);
					config.set(uuidString + ".rankedsolo", eloSolo);
					config.set(uuidString + ".rankedduplas", eloDuplas);
					config.set(uuidString + ".ranked1v1", elo1v1);
					config.set(uuidString + ".ranked4s", elo4v4);
					config.set(uuidString + ".ranked2v2cm", elo2v2CM);
					config.set(uuidString + ".mvp", mvpCount);
				}
				config.save(playerDataFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	public FileConfiguration getPlayerData() {
		return playerData;
	}

	public Map<UUID, Boolean> getDisplayPreferences() {
		return displayPreferences;
	}

	private void loadDisplayPreferences() {
		Plugin bedWarsPlugin = Bukkit.getPluginManager().getPlugin("BedWars2023");
		File dataFolder = new File(bedWarsPlugin.getDataFolder(), "Addons/Ranked");
		File file = new File(dataFolder, "displays.yml");
		if (!file.exists()) {
			return;
		}
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		if (!config.contains("ranks")) {
			return;
		}
		for (String uuidString : config.getConfigurationSection("ranks").getKeys(false)) {
			try {
				UUID uuid = UUID.fromString(uuidString);
				boolean display = config.getBoolean("ranks." + uuidString);
				displayPreferences.put(uuid, display);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
	}

	public void saveDisplayPreferences() {
		Plugin bedWarsPlugin = Bukkit.getPluginManager().getPlugin("BedWars2023");
		File dataFolder = new File(bedWarsPlugin.getDataFolder(), "Addons/Ranked");
		File file = new File(dataFolder, "displays.yml");
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		for (Map.Entry<UUID, Boolean> entry : displayPreferences.entrySet()) {
			config.set("ranks." + entry.getKey().toString(), entry.getValue());
		}
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static EloManager getEloManager() {
		return eloManager;
	}
}