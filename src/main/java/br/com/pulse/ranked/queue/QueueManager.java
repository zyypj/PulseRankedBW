package br.com.pulse.ranked.queue;

import br.com.pulse.ranked.Main;
import br.com.pulse.ranked.QueueAPI;
import br.com.pulse.ranked.elo.EloManager;
import com.tomkeuper.bedwars.api.BedWars;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueueManager implements QueueAPI {

    private final Main plugin;
    private final EloManager eloManager;
    private final Map<String, List<Player>> gameQueue;
    BedWars bedwarsAPI = Bukkit.getServicesManager().getRegistration(BedWars.class).getProvider();

    public QueueManager(Main plugin, EloManager eloManager) {
        this.plugin = plugin;
        this.eloManager = eloManager;
        this.gameQueue = new HashMap<>();
    }

    public void joinQueue(Player player, String gameType) {
        if (bedwarsAPI.getArenaUtil().isPlaying(player)) {
            player.sendMessage("§cVocê já está jogando!");
            player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 1);
            return;
        }
        List<Player> queue = gameQueue.computeIfAbsent(gameType, k -> new ArrayList<>());
        queue.add(player);
        player.sendMessage("");
        player.sendMessage("§7Você entrou na fila do modo: §5" + gameType);
        player.sendMessage("§7Digite §5/leavequeue §7para sair da fila.");
        player.sendMessage("");
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
        checkQueue(gameType);
    }

    public List<Player> getQueue(String gameType) {
        return gameQueue.getOrDefault(gameType, new ArrayList<>());
    }

    public void leaveQueue(Player player) {
        for (List<Player> queue : gameQueue.values()) {
            if (queue.contains(player)) {
                queue.remove(player);
                player.sendMessage("");
                player.sendMessage("§7Você saiu da fila.");
                player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
                return;
            }
        }
        player.sendMessage("§cVocê não está na fila.");
        player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 1);
    }

    private void checkQueue(String gameType) {
        List<Player> queue = gameQueue.get(gameType);
        if (queue != null && queue.size() >= 2) {
            List<Player> players = new ArrayList<>(queue.subList(0, 2));
            queue.removeAll(players);
            startGame(players, gameType);
        }
    }

    private void startGame(List<Player> players, String gameType) {
        for (Player player : players) {
            leaveQueue(player);
            if (bedwarsAPI.getArenaUtil().isPlaying(player)) {
                player.sendMessage("§cVocê já está jogando!");
                player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 1);
                return;
            }

            player.sendMessage("§7Partida Encontrada: §5" + gameType);
            player.sendMessage("§7Conectando...");
            player.sendMessage("");
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                bedwarsAPI.getArenaUtil().joinRandomFromGroup(player, gameType);
                if (bedwarsAPI.getArenaUtil().isPlaying(player)) {
                    player.sendMessage("");
                    player.sendMessage("§7Você entrou em uma partida ranqueada!");
                    player.sendMessage("§7Modo: §5" + gameType);
                    player.sendMessage("");
                    player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
                } else {
                    player.sendMessage("");
                    player.sendMessage("§cNão foi possível entrar na partida!");
                    player.sendMessage("§cRelogue e tente novamente.");
                    player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 1);
                }
            }, 40L);
        }
    }
}

