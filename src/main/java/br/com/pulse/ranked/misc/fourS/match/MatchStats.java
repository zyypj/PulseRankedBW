package br.com.pulse.ranked.misc.fourS.match;

import br.com.pulse.ranked.Main;
import com.github.syncwrld.prankedbw.bw4sbot.api.Ranked4SApi;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MatchStats {

    private final Main plugin;
    private File matchsFile;
    private FileConfiguration matchsConfig;

    Ranked4SApi api = Bukkit.getServicesManager().getRegistration(Ranked4SApi.class).getProvider();

    public MatchStats(Main plugin) {
        this.plugin = plugin;
        createMatchsFile();
    }

    private void createMatchsFile() {
        Plugin bedWarsPlugin = Bukkit.getPluginManager().getPlugin("BedWars2023");

        if (bedWarsPlugin != null) {
            matchsFile = new File(bedWarsPlugin.getDataFolder(), "Addons/Ranked/Matchs.yml");
            if (!matchsFile.exists()) {
                try {
                    matchsFile.getParentFile().mkdirs();
                    matchsFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            matchsConfig = YamlConfiguration.loadConfiguration(matchsFile);
        } else {
            plugin.getLogger().severe("BedWars2023 plugin not found!");
        }
    }

    public FileConfiguration getConfig() {
        return matchsConfig;
    }

    public void saveMatch(String id,String map, List<String> team1, List<String> team2, List<String> topKills, List<String> topBedBreaking) {

        matchsConfig.set(id + ".Mapa", map);
        matchsConfig.set(id + ".Data", new SimpleDateFormat("dd/MM/yy").format(new Date()));
        matchsConfig.set(id + ".Time1", team1);
        matchsConfig.set(id + ".Time2", team2);
        matchsConfig.set(id + ".TopKillsFinais", topKills);
        matchsConfig.set(id + ".TopBedBreaking", topBedBreaking);

        try {
            matchsConfig.save(matchsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getTopKills(Map<String, Integer> playerKills) {
        List<String> topKills = new ArrayList<>();

        LinkedHashMap<String, Integer> sortedKills = new LinkedHashMap<>();
        playerKills.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> sortedKills.put(x.getKey(), x.getValue()));

        int entryIndex = 0;
        for (Map.Entry<String, Integer> sortedEntry : sortedKills.entrySet()) {
            if (entryIndex == 0) {
                topKills.add("§61º " + sortedEntry.getKey() + " §7- §5" + sortedEntry.getValue());
            } else if (entryIndex == 1) {
                topKills.add("§82º " + sortedEntry.getKey() + " §7- §5" + sortedEntry.getValue());
            } else if (entryIndex == 2) {
                topKills.add("§43º " + sortedEntry.getKey() + " §7- §5" + sortedEntry.getValue());
                break;
            }
            entryIndex++;
        }

        return topKills;
    }

    public List<String> getTopBedBreaking(Map<String, Integer> playerBedsDestroyed) {
        List<String> topBedBreaking = new ArrayList<>();

        LinkedHashMap<String, Integer> sortedBeds = new LinkedHashMap<>();
        playerBedsDestroyed.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> sortedBeds.put(x.getKey(), x.getValue()));

        for (Map.Entry<String, Integer> entry : sortedBeds.entrySet()) {
            topBedBreaking.add(entry.getKey() + " §7- §5" + entry.getValue());
        }

        return topBedBreaking;
    }

    public void reloadConfig() {
        if (matchsFile != null) {
            matchsConfig = YamlConfiguration.loadConfiguration(matchsFile);
        }
    }
}