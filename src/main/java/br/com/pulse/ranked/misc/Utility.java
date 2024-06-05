package br.com.pulse.ranked.misc;

import br.com.pulse.ranked.Main;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class Utility {

    public static String c(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String p(Player player, String text) {
        return c(PlaceholderAPI.setPlaceholders(player, text));
    }

    public static String getMsg(Player player, String path) {
        return p(player, Main.getBedWars().getPlayerLanguage(player).getString(path));
    }

    public static List<String> getListMsg(Player player, String path) {
        return Main.getBedWars().getPlayerLanguage(player).getList(path).stream().map(s -> p(player, s)).collect(Collectors.toList());
    }
}
