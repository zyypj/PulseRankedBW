package br.com.pulse.ranked.api;

import com.tomkeuper.bedwars.api.arena.IArena;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PulseRankedAPI {

    // EloManager
    int getElo(UUID playerUUID, String type);

    int getElo(Player player, String type);

    void setElo(UUID playerUUID, String type, int elo);

    void setElo(Player player, String type, int elo);

    void addElo(UUID playerUUID, int eloChange, String type);

    void addElo(Player player, int eloChange, String type);

    int getMvpCount(Player player);

    String getRank(int elo);

    void saveData();

    // MVPManager
    void addFinalKillsPoints(Player player, IArena arena);

    void addBedBreakingPoints(Player player, IArena arena);

    Player determineMVP(IArena arena);

    // QueueManager
    void joinQueue(Player player, String gameType);

    void leaveQueue(Player player);

    // MatchStats
    void saveMatch(String id, String map, String group, List<String> team1, List<String> team2, Player mvp, List<String> topKills, List<String> topBedBreaking);

    List<String> getTopKills(Map<String, Integer> playerKills);

    List<String> getTopBedBreaking(Map<String, Integer> playerBedsDestroyed);

    String getMatch(String matchId);

    String getMatchValue(String matchId, String value);
}
