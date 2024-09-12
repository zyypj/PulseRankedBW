package br.com.pulse.ranked.api.impl;

import br.com.pulse.ranked.api.PulseRankedAPI;
import br.com.pulse.ranked.elo.EloManager;
import br.com.pulse.ranked.mvp.MVPManager;
import br.com.pulse.ranked.queue.QueueManager;
import br.com.pulse.ranked.ranked_bedwars.match.MatchStats;
import com.tomkeuper.bedwars.api.arena.IArena;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PulseRankedAPIImpl implements PulseRankedAPI {

    private final EloManager eloManager = EloManager.getEloManager();
    private final MVPManager mvpManager = MVPManager.getMvpManager();
    private final QueueManager queueManager = QueueManager.getQueueManager();
    private final MatchStats matchStats = MatchStats.getMatchStats();

    // EloManager
    @Override
    public int getElo(UUID playerUUID, String type) {
        return eloManager.getElo(playerUUID, type);
    }

    @Override
    public int getElo(Player player, String type) {
        UUID playerUUID = player.getUniqueId();
        return eloManager.getElo(playerUUID, type);
    }

    @Override
    public void setElo(UUID playerUUID, String type, int elo) {
        eloManager.setElo(playerUUID, type, elo);
    }

    @Override
    public void setElo(Player player, String type, int elo) {
        UUID playerUUID = player.getUniqueId();
        eloManager.setElo(playerUUID, type, elo);
    }

    @Override
    public void addElo(UUID playerUUID, int eloChange, String type) {
        eloManager.addElo(playerUUID, eloChange, type);
    }

    @Override
    public void addElo(Player player, int eloChange, String type) {
        UUID playerUUID = player.getUniqueId();
        eloManager.addElo(playerUUID, eloChange, type);
    }

    @Override
    public int getMvpCount(Player player) {
        return eloManager.getMvp(player);
    }

    @Override
    public String getRank(int elo) {
        return eloManager.getRank(elo);
    }

    @Override
    public void saveData() {
        eloManager.savePlayerData();
        eloManager.saveDisplayPreferences();
    }

    // MVPManager
    @Override
    public void addFinalKillsPoints(Player player, IArena arena) {
        mvpManager.addFinalKillsPoints(player, arena);
    }

    @Override
    public void addBedBreakingPoints(Player player, IArena arena) {
        mvpManager.addBedBreakingPoints(player, arena);
    }

    @Override
    public Player determineMVP(IArena arena) {
        mvpManager.reset(arena);
        return mvpManager.determineMVP(arena);
    }

    // QueueManager
    @Override
    public void joinQueue(Player player, String gameType) {
        queueManager.joinQueue(player, gameType);
    }

    @Override
    public void leaveQueue(Player player) {
        queueManager.leaveQueue(player);
    }

    // MatchStats
    @Override
    public void saveMatch(String id, String map, String group, List<String> team1, List<String> team2, Player mvp, List<String> topKills, List<String> topBedBreaking) {
        matchStats.saveMatch(id, map, group, team1, team2, mvp, topKills, topBedBreaking);
    }

    @Override
    public List<String> getTopKills(Map<String, Integer> playerKills) {
        return matchStats.getTopKills(playerKills);
    }

    @Override
    public List<String> getTopBedBreaking(Map<String, Integer> playerBedsDestroyed) {
        return matchStats.getTopBedBreaking(playerBedsDestroyed);
    }

    @Override
    public String getMatch(String matchId) {
        return matchStats.getMatch(matchId);
    }

    @Override
    public String getMatchValue(String matchId, String value) {
        return matchStats.getMatchValue(matchId, value);
    }
}
