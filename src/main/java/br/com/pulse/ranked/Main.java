package br.com.pulse.ranked;

import br.com.pulse.ranked.elo.EloListener;
import br.com.pulse.ranked.elo.EloManager;
import br.com.pulse.ranked.elo.commands.EloCommand;
import br.com.pulse.ranked.misc.RankCommand;
import br.com.pulse.ranked.queue.JoinQueueCommand;
import br.com.pulse.ranked.queue.LeaveQueueCommand;
import br.com.pulse.ranked.queue.QueueManager;
import br.com.pulse.ranked.util.BedWars2023;
import com.tomkeuper.bedwars.api.BedWars;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class Main extends JavaPlugin {

    private static EloManager eloManager;
    BedWars bedwarsAPI = Bukkit.getServicesManager().getRegistration(BedWars.class).getProvider();

    @Override
    public void onEnable() {

        if (Bukkit.getPluginManager().getPlugin("BedWars2023") == null) {
            getLogger().severe("BedWars2023 was not found. Disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Obtém o provedor da API BedWars
        RegisteredServiceProvider<BedWars> rsp = Bukkit.getServicesManager().getRegistration(BedWars.class);
        if (rsp == null || rsp.getProvider() == null) {
            getLogger().severe("API BedWars não encontrada. Desabilitando...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        bedwarsAPI = rsp.getProvider();

        Plugin bedWarsPlugin = Bukkit.getPluginManager().getPlugin("BedWars2023");

        BedWars2023 bedWars2023 = new BedWars2023(this);
        bedwarsAPI.getAddonsUtil().registerAddon(bedWars2023);

        File dataFolder = new File(bedWarsPlugin.getDataFolder(), "Addons/Ranked");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        File playerDataFile = new File(dataFolder, "playersElo.yml");
        if (!playerDataFile.exists()) {
            try {
                playerDataFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Erro ao criar o arquivo playersElo.yml: " + e.getMessage());
            }
        }

        FileConfiguration playerData = YamlConfiguration.loadConfiguration(playerDataFile);

        QueueManager queueManager = new QueueManager(this, eloManager);
        eloManager = new EloManager(this, playerData);

        Bukkit.getPluginManager().registerEvents(new JoinQueueCommand(queueManager, eloManager), this);
        Bukkit.getPluginManager().registerEvents(new EloListener(eloManager, this, playerData), this);

        getCommand("joinqueue").setExecutor(new JoinQueueCommand(queueManager, eloManager));
        getCommand("leavequeue").setExecutor(new LeaveQueueCommand(queueManager));
        getCommand("rank").setExecutor(new RankCommand(eloManager));
        getCommand("elo").setExecutor(new EloCommand(eloManager));

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            Bukkit.getScheduler().runTaskLater(this, () ->
                    getLogger().info("Hook to PlaceholderAPI support!"), 20L);
            new Placeholder(this, eloManager).register();
        }

    }

    @Override
    public void onDisable() {
        eloManager.savePlayerData();
        HandlerList.unregisterAll();
    }

    public static Main getPlugins() {
        return getPlugin(Main.class);
    }

}
