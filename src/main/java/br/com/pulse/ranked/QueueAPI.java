package br.com.pulse.ranked;

import org.bukkit.entity.Player;

import java.util.List;

public interface QueueAPI {

    void joinQueue(Player player, String gameType);

    void leaveQueue(Player player);

    List<Player> getQueue(String gameType);

}
