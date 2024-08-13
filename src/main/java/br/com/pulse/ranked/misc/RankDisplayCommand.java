package br.com.pulse.ranked.misc;

import br.com.pulse.ranked.elo.EloManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.UUID;

public class RankDisplayCommand implements CommandExecutor {

    private final EloManager eloManager;
    private final FileConfiguration config;

    public RankDisplayCommand(EloManager eloManager) {
        Plugin bedWarsPlugin = Bukkit.getPluginManager().getPlugin("BedWars2023");
        File dataFolder = new File(bedWarsPlugin.getDataFolder(), "Addons/Ranked");
        File file = new File(dataFolder, "displays.yml");
        this.eloManager = eloManager;
        this.config = YamlConfiguration.loadConfiguration(file);
        loadDisplay();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length != 1) {
            player.sendMessage("§cUse /rankdisplay <on/off/change>");
            return true;
        }

        UUID playerUUID = player.getUniqueId();

        if (args[0].equalsIgnoreCase("on")) {
            eloManager.getDisplayPreferences().put(playerUUID, true);
            player.sendMessage("§aA exibição do seu rank está ativada.");
            eloManager.saveDisplayPreferences();
            return true;
        }

        if (args[0].equalsIgnoreCase("off")) {
            eloManager.getDisplayPreferences().put(playerUUID, false);
            player.sendMessage("§aA exibição do seu rank está desativada.");
            eloManager.saveDisplayPreferences();
            return true;
        }

        if (args[0].equalsIgnoreCase("change")) {
            boolean currentStatus = eloManager.getDisplayPreferences().getOrDefault(playerUUID, true);
            eloManager.getDisplayPreferences().put(playerUUID, !currentStatus);
            player.sendMessage("§aA exibição do seu rank está " + (!currentStatus ? "ativada" : "desativada") + ".");
            eloManager.saveDisplayPreferences();
            return true;
        }

        player.sendMessage("§cUse /rankdisplay <on/off/change>");
        return true;
    }

    public void loadDisplay() {
        if (!config.contains("ranks")) {
            return;
        }
        for (String uuidString : config.getConfigurationSection("ranks").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                boolean display = config.getBoolean("ranks." + uuidString);
                eloManager.getDisplayPreferences().put(uuid, display);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }
}