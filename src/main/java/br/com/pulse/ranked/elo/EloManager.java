package br.com.pulse.ranked.elo;

import br.com.pulse.ranked.EloAPI;
import br.com.pulse.ranked.Main;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class EloManager implements EloAPI {

    private final FileConfiguration playerData;

    public EloManager(Main plugin, FileConfiguration playerData) {
        this.playerData = playerData;
    }

    public int getElo(UUID playerUUID, String type) {
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

        setElo(playerUUID, type, newElo);
        savePlayerData();
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
}

