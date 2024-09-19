package br.com.pulse.ranked.queue;

import br.com.pulse.ranked.Main;
import br.com.pulse.ranked.elo.EloManager;
import com.tomkeuper.bedwars.api.BedWars;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;

public class QueueManager {

    private static final int TICKS_PER_SECOND = 20;
    private static final int QUEUE_CHECK_INTERVAL = 5; // segundos
    private static final int MAX_QUEUE_TIME = 300; // segundos

    private final Main plugin;
    private final EloManager eloManager;
    private final Map<String, List<PlayerQueue>> game1v1Queue;
    private final Map<String, List<Player>> gameQueue;
    private final BedWars bedwarsAPI;
    private static QueueManager queueManager;

    public QueueManager(Main plugin, EloManager eloManager) {
        this.plugin = plugin;
        this.eloManager = eloManager;
        this.gameQueue = new WeakHashMap<>();
        this.game1v1Queue = new WeakHashMap<>();
        this.bedwarsAPI = Bukkit.getServicesManager().getRegistration(BedWars.class).getProvider();
        queueManager = this;
        startQueueTask();
    }

    public void joinQueue(Player player, String gameType) {
        if (bedwarsAPI.getArenaUtil().isPlaying(player)) {
            player.sendMessage("§cVocê já está jogando!");
            player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 1);
            return;
        }

        if (gameType.equalsIgnoreCase("Ranked1v1")) {
            int playerElo = eloManager.getElo(player.getUniqueId(), gameType.toLowerCase());
            List<PlayerQueue> queue = game1v1Queue.computeIfAbsent(gameType, k -> new ArrayList<>());
            PlayerQueue playerQueue = new PlayerQueue(player, playerElo);
            queue.add(playerQueue);
            notifyPlayerQueueJoined(player, gameType);
            handleRanked1v1Queue(playerQueue);
            return;
        }

        List<Player> queue = gameQueue.computeIfAbsent(gameType, k -> new ArrayList<>());
        queue.add(player);
        notifyPlayerQueueJoined(player, gameType);
        checkQueue(gameType);
    }

    private void handleRanked1v1Queue(PlayerQueue playerQueue) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            searchForBalancedMatch(playerQueue);
        }, 0, QUEUE_CHECK_INTERVAL * TICKS_PER_SECOND);
    }


    private void startQueueTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (String gameType : gameQueue.keySet()) {
                List<PlayerQueue> queue = game1v1Queue.get(gameType);
                if (queue == null) continue;
                List<PlayerQueue> queueCopy = new ArrayList<>(queue);
                queueCopy.removeIf(playerQueue -> {
                    if ((System.currentTimeMillis() - playerQueue.getJoinTime()) >= MAX_QUEUE_TIME * 1000) {
                        leaveQueue(playerQueue.getPlayer());
                        return true;
                    }
                    searchForMatch(playerQueue, gameType);
                    return false;
                });
            }
        }, 0, QUEUE_CHECK_INTERVAL * TICKS_PER_SECOND);
    }

    private void searchForBalancedMatch(PlayerQueue playerQueue) {
        int baseElo = playerQueue.getElo();
        int timeInQueue = (int) ((System.currentTimeMillis() - playerQueue.getJoinTime()) / 1000);
        int range = (timeInQueue / QUEUE_CHECK_INTERVAL) * 50;

        List<PlayerQueue> queue = game1v1Queue.get("Ranked1v1");
        Optional<PlayerQueue> match = queue.stream()
                .filter(other -> other != playerQueue && Math.abs(other.getElo() - baseElo) <= range)
                .findFirst();
        if (match.isPresent()) {
            startGame1v1(Arrays.asList(playerQueue, match.get()), "Ranked1v1");
        } else {
            notifyPlayerSearching(playerQueue, baseElo, range, "Ranked 1v1");
        }
    }

    private void searchForMatch(PlayerQueue playerQueue, String gameType) {
        int baseElo = playerQueue.getElo();
        int timeInQueue = (int) ((System.currentTimeMillis() - playerQueue.getJoinTime()) / 1000);
        int range = (timeInQueue / QUEUE_CHECK_INTERVAL) * 50;

        List<PlayerQueue> queue = game1v1Queue.get(gameType);
        Optional<PlayerQueue> match = queue.stream()
                .filter(other -> other != playerQueue && Math.abs(other.getElo() - baseElo) <= range)
                .findFirst();
        if (match.isPresent()) {
            startGame1v1(Arrays.asList(playerQueue, match.get()), gameType);
        } else {
            notifyPlayerSearching(playerQueue, baseElo, range, gameType);
        }
    }

    private void startGame1v1(List<PlayerQueue> players, String gameType) {
        players.forEach(playerQueue -> leaveQueue(playerQueue.getPlayer())); // Remove da fila antes de iniciar a partida
        players.forEach(playerQueue -> startPlayerGame(playerQueue.getPlayer(), gameType));
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
        players.forEach(player -> startPlayerGame(player, gameType));
    }

    private void startPlayerGame(Player player, String gameType) {
        player.sendMessage("§7Partida Encontrada: §5" + gameType);
        player.sendMessage("§7Conectando...");
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            bedwarsAPI.getArenaUtil().joinRandomFromGroup(player, gameType);
            if (bedwarsAPI.getArenaUtil().isPlaying(player)) {
                player.sendMessage("§7Você entrou em uma partida ranqueada!");
                Bukkit.getScheduler().runTaskLater(plugin, () ->
                        bedwarsAPI.getArenaUtil().getArenaByPlayer(player).getStartingTask().setCountdown(5), 20L);
            } else {
                player.sendMessage("§cNão foi possível entrar na partida!");
            }
        }, 40L);
    }

    public List<Player> getQueue(String gameType) {
        return gameQueue.getOrDefault(gameType, new ArrayList<>());
    }

    public void leaveQueue(Player player) {
        gameQueue.values().forEach(queue -> queue.remove(player));
        game1v1Queue.values().forEach(queue -> queue.removeIf(playerQueue -> playerQueue.getPlayer().equals(player)));
        player.sendMessage("§7Você saiu da fila.");
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
    }

    private void notifyPlayerQueueJoined(Player player, String gameType) {
        player.sendMessage("§7Você entrou na fila do modo: §5" + gameType);
        player.sendMessage("§7Digite §5/leavequeue §7para sair da fila.");
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
    }

    private void notifyPlayerSearching(PlayerQueue playerQueue, int baseElo, int range, String gameType) {
        playerQueue.getPlayer().sendMessage("§c§lPulse Ranked");
        playerQueue.getPlayer().sendMessage("§fProcurando partida...");
        playerQueue.getPlayer().sendMessage("§fModo: §5" + gameType);
        int minElo = baseElo - range;
        int maxElo = baseElo + range;
        playerQueue.getPlayer().sendMessage("§fElo: §5[§7" + minElo + "§f-§7" + maxElo + "§5]");
    }

    public static QueueManager getQueueManager() {
        return queueManager;
    }
}

class PlayerQueue {
    private final Player player;
    private final int elo;
    private final long joinTime;

    public PlayerQueue(Player player, int elo) {
        this.player = player;
        this.elo = elo;
        this.joinTime = System.currentTimeMillis();
    }

    public Player getPlayer() {
        return player;
    }

    public int getElo() {
        return elo;
    }

    public long getJoinTime() {
        return joinTime;
    }
}