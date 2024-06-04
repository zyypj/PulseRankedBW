package br.com.pulse.ranked.util;

import br.com.pulse.ranked.Main;
import com.tomkeuper.bedwars.api.addon.Addon;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class BedWars2023 extends Addon {

    public BedWars2023(Main plugin) {
    }

    @Override
    public String getAuthor() {
        return "tadeu";
    }

    @Override
    public Plugin getPlugin() {
        return Main.getPlugins();
    }

    @Override
    public String getVersion() {
        return getPlugin().getDescription().getVersion();
    }

    @Override
    public String getName() {
        return getPlugin().getDescription().getName();
    }

    @Override
    public String getDescription() {
        return getPlugin().getDescription().getDescription();
    }

    @Override
    public void load() {
        loadListeners();
        loadCommands();
    }

    @Override
    public void unload() {
        Bukkit.getPluginManager().disablePlugin(getPlugin());
    }

    public void loadListeners() {
        Bukkit.getConsoleSender().sendMessage("&eLoading Listeners...");
        Bukkit.getConsoleSender().sendMessage("&aListeners loaded!");
    }

    public void loadCommands() {
        Bukkit.getConsoleSender().sendMessage("&eLoading Commands...");
        Bukkit.getConsoleSender().sendMessage("&aCommands loaded!");
    }
}

