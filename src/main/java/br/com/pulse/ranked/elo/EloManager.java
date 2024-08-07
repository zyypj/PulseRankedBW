package br.com.pulse.ranked.elo;

import br.com.pulse.ranked.EloAPI;
import com.github.syncwrld.prankedbw.bw4sbot.api.Ranked4SApi;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EloManager implements EloAPI {

    private final FileConfiguration playerData;
    private final Map<UUID, Boolean> displayPreferences;

    Ranked4SApi api = Bukkit.getServicesManager().getRegistration(Ranked4SApi.class).getProvider();

    public EloManager(FileConfiguration playerData) {
        this.playerData = playerData;
        this.displayPreferences = new HashMap<>();
        loadDisplayPreferences();
    }

    public int getElo(UUID playerUUID, String type) {
        if (type.equalsIgnoreCase("geral")) {
            Player player = Bukkit.getPlayer(playerUUID);
            int elo1v1 = getElo(playerUUID, "ranked1v1");
            int elo4v4 = api.getElo(player);
            int elo2v2cm = getElo(playerUUID, "ranked2v2cm");
            return (elo1v1 + elo4v4 + elo2v2cm) / 3;
        }
        return playerData.getInt(playerUUID + "." + type, 0);
    }

    public void setElo(UUID playerUUID, String type, int elo) {
        playerData.set(playerUUID.toString() + "." + type, elo);
        savePlayerData();
    }

    public void addElo(UUID playerUUID, int eloChange, String type) {
        int currentElo = getElo(playerUUID, type);
        int newElo = currentElo + eloChange;

        if (newElo < 0) {
            newElo = 0; // Elo não pode ser negativo
        }

        String oldRank = getRank(currentElo);
        String newRank = getRank(newElo);

        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            if (!oldRank.equalsIgnoreCase(newRank)) {
                int oldRankPriority = getRankPriority(oldRank);
                int newRankPriority = getRankPriority(newRank);
                if (newRankPriority < oldRankPriority) {
                    player.sendMessage("§c§lVocê desceu de rank!");
                } else {
                    player.sendMessage("§a§lVocê evoluiu de Rank!");
                }
                player.sendMessage("§aSeu novo rank é: " + newRank);
                player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
            }
        }

        setElo(playerUUID, type, newElo);
        savePlayerData();
    }

    public int getMvp(Player player) {
        return getPlayerData().getInt(player.getUniqueId() + ".mvp", 0);
    }

    public String getRank(int elo) {
        if (elo <= 50) {
            return "§4[Bronze III]";
        } else if (elo < 100) {
            return "§4[Bronze II]";
        } else if (elo < 150) {
            return "§4[Bronze I]";
        } else if (elo < 250) {
            return "§8[Prata III]";
        } else if (elo < 350) {
            return "§8[Prata II]";
        } else if (elo < 450) {
            return "§8[Prata I]";
        } else if (elo < 600) {
            return "§6[Ouro III]";
        } else if (elo < 700) {
            return "§6[Ouro II]";
        } else if (elo < 800) {
            return "§6[Ouro I]";
        } else if (elo < 900) {
            return "§b[Diamante III]";
        } else if (elo < 1100) {
            return "§b[Diamante II]";
        } else {
            return "§b[Diamante I]";
        }
    }

    public int getRankPriority(String rank) {
        return switch (rank) {
            case "§4[Bronze III]" -> 1;
            case "§4[Bronze II]" -> 2;
            case "§4[Bronze I]" -> 3;
            case "§8[Prata III]" -> 4;
            case "§8[Prata II]" -> 5;
            case "§8[Prata I]" -> 6;
            case "§6[Ouro III]" -> 7;
            case "§6[Ouro II]" -> 8;
            case "§6[Ouro I]" -> 9;
            case "§b[Diamante III]" -> 10;
            case "§b[Diamante II]" -> 11;
            case "§b[Diamante I]" -> 12;
            default -> Integer.MAX_VALUE;
        };
    }

    public void savePlayerData() {
        File dataFolder = new File(Bukkit.getPluginManager().getPlugin("BedWars2023").getDataFolder(), "Addons/Ranked");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        File playerDataFile = new File(dataFolder, "playersElo.yml");
        try {
            YamlConfiguration config = new YamlConfiguration();
            for (String uuidString : playerData.getKeys(false)) {

                // Define o elo inicial como 0 em todos os modos
                int eloSolo = 0;
                int eloDuplas = 0;
                int elo1v1 = 0;
                int elo4v4 = 0;
                int elo2v2CM = 0;
                int eloGeral = 0;
                int mvpCount = 0;

                // Se o jogador já tiver um elo registrado, mantém o elo atual
                if (playerData.contains(uuidString + ".rankedsolo")) {
                    eloSolo = playerData.getInt(uuidString + ".rankedsolo");
                }
                if (playerData.contains(uuidString + ".rankedduplas")) {
                    eloDuplas = playerData.getInt(uuidString + ".rankedduplas");
                }
                if (playerData.contains(uuidString + ".ranked1v1")) {
                    elo1v1 = playerData.getInt(uuidString + ".ranked1v1");
                }
                if (playerData.contains(uuidString + ".ranked4s")) {
                    elo4v4 = playerData.getInt(uuidString + ".ranked4s");
                }
                if (playerData.contains(uuidString + ".ranked2v2cm")) {
                    elo2v2CM = playerData.getInt(uuidString + ".ranked2v2cm");
                }
                if (playerData.contains(uuidString + ".rankedgeral")) {
                    eloGeral = playerData.getInt(uuidString + ".rankedgeral");
                }
                if (playerData.contains(uuidString + ".mvp")) {
                    mvpCount = playerData.getInt(uuidString + ".mvp");
                }

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
}