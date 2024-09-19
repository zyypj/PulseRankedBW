package br.com.pulse.ranked;

import br.com.pulse.ranked.api.impl.PulseRankedAPIImpl;
import br.com.pulse.ranked.elo.EloListener;
import br.com.pulse.ranked.elo.EloManager;
import br.com.pulse.ranked.elo.commands.EloCommand;
import br.com.pulse.ranked.misc.listeners.ListenersMisc;
import br.com.pulse.ranked.rank.RankCommand;
import br.com.pulse.ranked.rank.RankDisplayCommand;
import br.com.pulse.ranked.ranked_bedwars.ForgeManager;
import br.com.pulse.ranked.ranked_bedwars.TeamManager;
import br.com.pulse.ranked.ranked_bedwars.match.MatchCommand;
import br.com.pulse.ranked.ranked_bedwars.match.MatchListener;
import br.com.pulse.ranked.misc.listeners.AntiLadder;
import br.com.pulse.ranked.misc.listeners.FireballListener;
import br.com.pulse.ranked.mvp.MVPCommand;
import br.com.pulse.ranked.mvp.MVPListener;
import br.com.pulse.ranked.mvp.MVPManager;
import br.com.pulse.ranked.tournament.TournamentCommand;
import br.com.pulse.ranked.queue.JoinQueueCommand;
import br.com.pulse.ranked.queue.LeaveQueueCommand;
import br.com.pulse.ranked.queue.QueueManager;
import br.com.pulse.ranked.support.Placeholder;
import com.tomkeuper.bedwars.api.server.VersionSupport;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class Main extends JavaPlugin {

    public static VersionSupport nms;
    private static PulseRankedAPIImpl pulseRankedAPIImpl;
    public static Main plugin;
    private EloManager eloManager;
    private QueueManager queueManager;
    private MVPManager mvpManager;

    private final String NMS_VERSION = Bukkit.getServer().getClass().getName().split("\\.")[3];
    private final String MINECRAFT_VERSION = Bukkit.getServer().getBukkitVersion().split("-")[0];

    @Override
    public void onLoad() {
        if (!loadServerSupport()) {
            this.getLogger().severe("Unsupported server software or Minecraft version: " + MINECRAFT_VERSION);
            return;
        }

        plugin = this;
    }

    @Override
    public void onEnable() {
        if (!loadDependencies()) return;

        initializeManagers();
        registerCommands();
        registerListeners();
        setupPlaceholderAPI();
        eloManager.savePlayerData();

        scheduleTasks();
    }

    @Override
    public void onDisable() {
        eloManager.savePlayerData();
        saveConfig();
    }

    private boolean loadServerSupport() {
        try {
            Class.forName("org.spigotmc.SpigotConfig");
        } catch (Exception e) {
            this.getLogger().severe("Unsupported server software.");
            return false;
        }

        try {
            Class<?> supportClass = Class.forName("com.tomkeuper.bedwars.support.version." + NMS_VERSION + "." + NMS_VERSION);
            nms = (VersionSupport) supportClass.getConstructor(Plugin.class, String.class).newInstance(this, NMS_VERSION);
        } catch (Exception e) {
            this.getLogger().severe("Failed to load server support for version: " + MINECRAFT_VERSION);
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean loadDependencies() {
        Plugin bedWarsPlugin = Bukkit.getPluginManager().getPlugin("BedWars2023");

        if (bedWarsPlugin == null || !bedWarsPlugin.isEnabled()) {
            Bukkit.getPluginManager().disablePlugin(this);
            return false;
        }

        setupDataFolder(bedWarsPlugin);
        return true;
    }

    private void setupDataFolder(Plugin bedWarsPlugin) {
        File dataFolder = new File(bedWarsPlugin.getDataFolder(), "Addons/Ranked");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File playerDataFile = new File(dataFolder, "playersElo.yml");
        if (!playerDataFile.exists()) {
            try {
                playerDataFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Error creating playersElo.yml: " + e.getMessage());
            }
        }
        FileConfiguration playerData = YamlConfiguration.loadConfiguration(playerDataFile);
        eloManager = new EloManager(playerData);
    }

    private void initializeManagers() {
        queueManager = new QueueManager(this, eloManager);
        mvpManager = new MVPManager();
        pulseRankedAPIImpl = new PulseRankedAPIImpl();
    }

    private void registerCommands() {
        getCommand("joinqueue").setExecutor(new JoinQueueCommand(queueManager, eloManager));
        getCommand("leavequeue").setExecutor(new LeaveQueueCommand(queueManager));
        getCommand("rank").setExecutor(new RankCommand(eloManager, eloManager.getDisplayPreferences()));
        getCommand("elo").setExecutor(new EloCommand(eloManager, eloManager.getDisplayPreferences()));
        getCommand("mvp").setExecutor(new MVPCommand(eloManager));
        getCommand("partida").setExecutor(new MatchCommand(this));
        getCommand("rankdisplay").setExecutor(new RankDisplayCommand(eloManager));
        getCommand("tournament").setExecutor(new TournamentCommand());
    }

    private void registerListeners() {
        registerEvents(
                new JoinQueueCommand(queueManager, eloManager),
                new EloListener(eloManager, this, eloManager.getPlayerData()),
                new AntiLadder(),
                new ForgeManager(this),
                new MVPListener(this, mvpManager, eloManager),
                new TeamManager(),
                new MatchListener(),
                new FireballListener(),
                new ListenersMisc(this)
        );
    }

    private void setupPlaceholderAPI() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            Bukkit.getScheduler().runTaskLater(this, () ->
                    getLogger().info("Hooked to PlaceholderAPI support!"), 20L);
            new Placeholder(this, eloManager, eloManager.getDisplayPreferences()).register();
        }
    }

    private void scheduleTasks() {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            getLogger().info("PulseRanked enabled");
            getLogger().info("Hooked to BedWars2023 and PlaceholderAPI");
        }, 60L);
    }

    public static void registerEvents(Listener... listeners) {
        Arrays.stream(listeners).forEach(l -> plugin.getServer().getPluginManager().registerEvents(l, plugin));
    }

    public static Main getInstance() {
        return plugin;
    }

    public static PulseRankedAPIImpl getAPI() {
        return pulseRankedAPIImpl;
    }

    public static void debug(String msg) {
        plugin.getLogger().info("[DEBUG] - " + msg);
    }
}