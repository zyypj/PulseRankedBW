package br.com.pulse.ranked;

import br.com.pulse.ranked.elo.EloListener;
import br.com.pulse.ranked.elo.EloManager;
import br.com.pulse.ranked.elo.commands.EloCommand;
import br.com.pulse.ranked.integrations.BedWars2023;
import br.com.pulse.ranked.integrations.IIntegration;
import br.com.pulse.ranked.misc.RankCommand;
import br.com.pulse.ranked.queue.JoinQueueCommand;
import br.com.pulse.ranked.queue.LeaveQueueCommand;
import br.com.pulse.ranked.queue.QueueManager;
import br.com.pulse.ranked.support.Placeholder;
import com.avaje.ebeaninternal.server.lib.util.NotFoundException;
import com.tomkeuper.bedwars.api.BedWars;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public final class Main extends JavaPlugin {

    public static BedWars bedWars;
    public static Main plugin;
    private static EloManager eloManager;
    private static QueueManager queueManager;

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        populateIntegrations(new BedWars2023(this, bedWars = Bukkit.getServicesManager().getRegistration(BedWars.class).getProvider()));

        Plugin bedWarsPlugin = Bukkit.getPluginManager().getPlugin("BedWars2023");

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

        queueManager = new QueueManager(this, eloManager);
        eloManager = new EloManager(this, playerData);

        registerEvents(new JoinQueueCommand(queueManager, eloManager), new EloListener(eloManager, this, playerData));

        getCommand("joinqueue").setExecutor(new JoinQueueCommand(queueManager, eloManager));
        getCommand("leavequeue").setExecutor(new LeaveQueueCommand(queueManager));
        getCommand("rank").setExecutor(new RankCommand(eloManager));
        getCommand("elo").setExecutor(new EloCommand(eloManager));

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            Bukkit.getScheduler().runTaskLater(this, () ->
                    getLogger().info("Hook to PlaceholderAPI support!"), 20L);
            new Placeholder(this, eloManager).register();
        }

        Bukkit.getScheduler().runTaskLater(this, () -> {
            getLogger().info("");
            getLogger().info("PulseRanked enabled");
            getLogger().info("Hook to BedWars2023 support");
            getLogger().info("Hook to Placeholder support");
            getLogger().info("");
        }, 60L);

    }

    @Override
    public void onDisable() {
        eloManager.savePlayerData();
    }

    private void populateIntegrations(IIntegration... integrations) {
        for (IIntegration integration : integrations) {
            if (!integration.enable()) {
                throw new NotFoundException("Plugin could not be enabled as on or more of the dependencies could not be hooked.");
            }
        }
    }

    public boolean isBedWarsInstalled(){
        return Bukkit.getPluginManager().getPlugin("BedWars2023") != null;
    }

    public static void registerEvents(Listener... listeners) {
        Arrays.stream(listeners).forEach(l -> plugin.getServer().getPluginManager().registerEvents(l, plugin));
    }

    public static BedWars getBedWars() {
        return bedWars;
    }

    public static Main getInstance(){
        return plugin;
    }

    public static void debug(String msg){
        plugin.getLogger().info("[DEBUG] - " + msg);
    }

    public static EloAPI getEloAPI() {
        return eloManager;
    }

    public static QueueAPI getQueueAPI() {
        return queueManager;
    }

}
