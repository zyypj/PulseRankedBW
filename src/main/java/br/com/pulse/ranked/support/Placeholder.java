package br.com.pulse.ranked.support;

import br.com.pulse.ranked.Main;
import br.com.pulse.ranked.elo.EloManager;
import com.github.syncwrld.prankedbw.bw4sbot.api.Ranked4SApi;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class Placeholder extends PlaceholderExpansion {

    private final Main plugin;
    private final EloManager eloManager;
    private final Map<UUID, Boolean> displayPreferences;
    Ranked4SApi api = Bukkit.getServicesManager().getRegistration(Ranked4SApi.class).getProvider();

    public Placeholder(Main plugin, EloManager eloManager, Map<UUID, Boolean> displayPreferences) {
        this.plugin = plugin;
        this.eloManager = eloManager;
        this.displayPreferences = displayPreferences;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        switch (identifier.toLowerCase()) {
            case "bwelo_2v2":
                return String.valueOf(eloManager.getElo(player.getUniqueId(), "ranked2v2cm"));
            case "mvp":
                return String.valueOf(eloManager.getMvp(player));
            case "bwelo_1v1":
                return String.valueOf(eloManager.getElo(player.getUniqueId(), "ranked1v1"));
            case "bwelo_4s":
                return String.valueOf(api.getElo(player));
            case "bwelo_solo":
                return String.valueOf(eloManager.getElo(player.getUniqueId(), "rankedsolo"));
            case "bwelo_duplas":
                return String.valueOf(eloManager.getElo(player.getUniqueId(), "rankeduplas"));
            case "bwelo_geral":
                int elo1v1 = eloManager.getElo(player.getUniqueId(), "ranked1v1");
                int elo4v4 = api.getElo(player);
                int elo2v2 = eloManager.getElo(player.getUniqueId(), "ranked2v2cm");
                int eloSolo = eloManager.getElo(player.getUniqueId(), "rankedsolo");
                int eloDuplas = eloManager.getElo(player.getUniqueId(), "rankedduplas");
                int eloGeral = (elo1v1 + elo2v2 + elo4v4) / 3;
                return String.valueOf(eloGeral);
            case "bwrank":
                int eloGeralRank = (eloManager.getElo(player.getUniqueId(), "ranked1v1") +
                        eloManager.getElo(player.getUniqueId(), "ranked4s") +
                        eloManager.getElo(player.getUniqueId(), "ranked2v2cm")) / 3;
                boolean displayTag = displayPreferences.getOrDefault(player.getUniqueId(), true);
                if (displayTag) {
                    return (eloManager.getRank(eloGeralRank));
                } else {
                    return "§c[Oculto]";
                }
            case "rank_status":
                return eloManager.getDisplayPreferences().getOrDefault(player.getUniqueId(), true) ? "Ativado" : "Desativado";
        }

        return null;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "bw2023ranked";
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


