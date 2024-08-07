package br.com.pulse.ranked;

import org.bukkit.entity.Player;

import java.util.UUID;

public interface EloAPI {

    int getElo(UUID playerUUID, String type);

    void setElo(UUID playerUUID, String type, int elo);

    void addElo(UUID playerUUID, int eloChange, String type);

    String getRank(int elo);

    int getMvp(Player player);

}
