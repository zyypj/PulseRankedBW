package br.com.pulse.ranked.mvp;

import com.tomkeuper.bedwars.api.arena.IArena;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MVPManager {
    private final Map<IArena, Map<UUID, Integer>> arenaMvpPoints;
    public static MVPManager mvpManager;

    public MVPManager() {
        this.arenaMvpPoints = new HashMap<>();
        mvpManager = this;
    }

    public void addFinalKillsPoints(Player player, IArena arena) {
        arenaMvpPoints.putIfAbsent(arena, new HashMap<>());
        Map<UUID, Integer> mvpPoints = arenaMvpPoints.get(arena);
        mvpPoints.putIfAbsent(player.getUniqueId(), 0);
        mvpPoints.put(player.getUniqueId(), mvpPoints.get(player.getUniqueId()) + 1);
    }

    public void addBedBreakingPoints(Player player, IArena arena) {
        arenaMvpPoints.putIfAbsent(arena, new HashMap<>());
        Map<UUID, Integer> mvpPoints = arenaMvpPoints.get(arena);
        mvpPoints.putIfAbsent(player.getUniqueId(), 0);
        mvpPoints.put(player.getUniqueId(), mvpPoints.get(player.getUniqueId()) + 2);
    }

    public Player determineMVP(IArena arena) {
        Map<UUID, Integer> mvpPoints = arenaMvpPoints.get(arena);
        if (mvpPoints == null) {
            return null;
        }

        UUID mvp = null;
        int maxPoints = 0;

        for (Map.Entry<UUID, Integer> entry : mvpPoints.entrySet()) {
            if (entry.getValue() > maxPoints) {
                maxPoints = entry.getValue();
                mvp = entry.getKey();
            }
        }

        return mvp != null ? Bukkit.getPlayer(mvp) : null;
    }

    public void reset(IArena arena) {
        arenaMvpPoints.remove(arena);
    }

    public static MVPManager getMvpManager() {
        return mvpManager;
    }
}
