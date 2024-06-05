package br.com.pulse.ranked.support;

import br.com.pulse.ranked.Main;
import br.com.pulse.ranked.elo.EloManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Placeholder extends PlaceholderExpansion {

    private final Main plugin;
    private final EloManager eloManager;

    public Placeholder(Main plugin, EloManager eloManager) {
        this.plugin = plugin;
        this.eloManager = eloManager;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        switch (identifier.toLowerCase()) {
            case "bwelo_1v1":
                return String.valueOf(eloManager.getElo(player.getUniqueId(), "ranked1v1"));
            case "bwelo_4v4":
                return String.valueOf(eloManager.getElo(player.getUniqueId(), "ranked4v4"));
            case "bwelo_solo":
                return String.valueOf(eloManager.getElo(player.getUniqueId(), "rankedsolo"));
            case "bwelo_duplas":
                return String.valueOf(eloManager.getElo(player.getUniqueId(), "rankeduplas"));
            case "bwelo_geral":
                int elo1v1 = eloManager.getElo(player.getUniqueId(), "ranked1v1");
                int elo4v4 = eloManager.getElo(player.getUniqueId(), "ranked4v4");
                int eloSolo = eloManager.getElo(player.getUniqueId(), "rankedsolo");
                int eloDuplas = eloManager.getElo(player.getUniqueId(), "rankedduplas");
                int eloGeral = (elo1v1 + elo4v4 + eloSolo + eloDuplas) / 4;
                return String.valueOf(eloGeral);
            case "bwrank":
                int eloGeralRank = (eloManager.getElo(player.getUniqueId(), "ranked1v1") +
                        eloManager.getElo(player.getUniqueId(), "ranked4v4") +
                        eloManager.getElo(player.getUniqueId(), "rankedsolo") +
                        eloManager.getElo(player.getUniqueId(), "rankedduplas")) / 4;
                return eloManager.getRank(eloGeralRank);
        }

        return null;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "bw1058ranked";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }
}


