package br.com.pulse.ranked.support;

import br.com.pulse.ranked.Main;
import com.tomkeuper.bedwars.api.addon.Addon;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class BW2023 extends Addon {

    public static BW2023 instance;

    public BW2023() {
        instance = this;
    }

    @Override
    public String getAuthor() {
        return Main.getInstance().getDescription().getAuthors().get(0);
    }

    @Override
    public Plugin getPlugin() {
        return Main.getInstance();
    }

    @Override
    public String getVersion() {
        return Main.getInstance().getDescription().getVersion();
    }

    @Override
    public String getDescription() {
        return Main.getInstance().getDescription().getDescription();
    }

    @Override
    public String getName() {
        return Main.getInstance().getDescription().getName();
    }

    @Override
    public void load() {
        Bukkit.getPluginManager().enablePlugin(Main.getInstance());
    }

    @Override
    public void unload() {
        Bukkit.getPluginManager().disablePlugin(Main.getInstance());
    }
}
