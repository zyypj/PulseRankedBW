package br.com.pulse.ranked;

import br.com.pulse.ranked.elo.EloListener;
import br.com.pulse.ranked.elo.EloManager;
import br.com.pulse.ranked.elo.commands.EloCommand;
import br.com.pulse.ranked.misc.RankCommand;
import br.com.pulse.ranked.misc.RankDisplayCommand;
import br.com.pulse.ranked.misc.fourS.ForgeManager;
import br.com.pulse.ranked.misc.fourS.TeamManager;
import br.com.pulse.ranked.misc.fourS.match.MatchCommand;
import br.com.pulse.ranked.misc.fourS.match.MatchListener;
import br.com.pulse.ranked.misc.listeners.AntiLadder;
import br.com.pulse.ranked.misc.listeners.FireballListener;
import br.com.pulse.ranked.misc.mvp.MVPCommand;
import br.com.pulse.ranked.misc.mvp.MVPListener;
import br.com.pulse.ranked.misc.mvp.MVPManager;
import br.com.pulse.ranked.queue.JoinQueueCommand;
import br.com.pulse.ranked.queue.LeaveQueueCommand;
import br.com.pulse.ranked.queue.QueueManager;
import br.com.pulse.ranked.support.Placeholder;
import com.tomkeuper.bedwars.api.BedWars;
import com.tomkeuper.bedwars.api.server.VersionSupport;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class Main extends JavaPlugin {

    public static BedWars bedWars;
    public static Main plugin;
    private static EloManager eloManager;
    private static QueueManager queueManager;

    public static VersionSupport nms;
    boolean serverSoftwareSupport = true;
    private static String nmsVersion = Bukkit.getServer().getClass().getName().split("\\.")[3];
    private static final String minecraftVersion = Bukkit.getServer().getBukkitVersion().split("-")[0];

    @Override
    public void onLoad() {
        try {
            Class.forName("org.spigotmc.SpigotConfig");
        } catch (Exception ignored) {
            this.getLogger().severe("I can't run on your server software. Please check:");
            this.getLogger().severe("https://gitlab.com/andrei1058/BedWars1058/wikis/compatibility");
            serverSoftwareSupport = false;
            return;
        }

        switch (minecraftVersion){
            case "1.20.4":
                nmsVersion = "v1_20_R3";
                break;
            case "1.20.6":
                nmsVersion = "v1_20_R5";
                break;
            default:
                break;
        }

        Class supp;
        try {
            supp = Class.forName("com.tomkeuper.bedwars.support.version." + nmsVersion + "." + nmsVersion);
        } catch (ClassNotFoundException e) {
            serverSoftwareSupport = false;
            this.getLogger().severe("I can't run on your version: " + minecraftVersion);
            return;
        }

        try {
            //noinspection unchecked
            nms = (VersionSupport) supp.getConstructor(Class.forName("org.bukkit.plugin.Plugin"), String.class).newInstance(this, nmsVersion);
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                 ClassNotFoundException e) {
            e.printStackTrace();
            serverSoftwareSupport = false;
            this.getLogger().severe("Could not load support for server version: " + minecraftVersion);
        }
        plugin = this;
    }

    @Override
    public void onEnable() {
        Plugin bedWarsPlugin = Bukkit.getPluginManager().getPlugin("BedWars2023");

        if (!bedWarsPlugin.isEnabled()) {
            Bukkit.getPluginManager().disablePlugin(this);
        }

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

        eloManager = new EloManager(playerData);
        queueManager = new QueueManager(this, eloManager);
        MVPManager mvpManager = new MVPManager();

        registerEvents(new JoinQueueCommand(queueManager, eloManager), new EloListener(eloManager, this, playerData),
        new AntiLadder(), new ForgeManager(this), new MVPListener(this, mvpManager, eloManager),
                new TeamManager(), new MatchListener(this), new FireballListener());

        getCommand("joinqueue").setExecutor(new JoinQueueCommand(queueManager, eloManager));
        getCommand("leavequeue").setExecutor(new LeaveQueueCommand(queueManager));
        getCommand("rank").setExecutor(new RankCommand(eloManager));
        getCommand("elo").setExecutor(new EloCommand(eloManager));
        getCommand("mvp").setExecutor(new MVPCommand(eloManager));
        getCommand("partida").setExecutor(new MatchCommand(this));
        getCommand("rankdisplay").setExecutor(new RankDisplayCommand(eloManager));

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            Bukkit.getScheduler().runTaskLater(this, () ->
                    getLogger().info("Hook to PlaceholderAPI support!"), 20L);
            new Placeholder(this, eloManager, eloManager.getDisplayPreferences()).register();
        }

        eloManager.savePlayerData();

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
