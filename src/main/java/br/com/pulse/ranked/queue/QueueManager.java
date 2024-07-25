package br.com.pulse.ranked.queue;

import br.com.pulse.ranked.Main;
import br.com.pulse.ranked.QueueAPI;
import br.com.pulse.ranked.elo.EloManager;
import com.tomkeuper.bedwars.api.BedWars;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class QueueManager implements QueueAPI {

    private final Main plugin;
    private final EloManager eloManager;
    private final Map<String, List<PlayerQueue>> gameQueue;
    BedWars bedwarsAPI = Bukkit.getServicesManager().getRegistration(BedWars.class).getProvider();

    public QueueManager(Main plugin, EloManager eloManager) {
        this.plugin = plugin;
        this.eloManager = eloManager;
        this.gameQueue = new ConcurrentHashMap<>();
        startQueueTask();
    }

    public void joinQueue(Player player, String gameType) {
        if (bedwarsAPI.getArenaUtil().isPlaying(player)) {
            player.sendMessage("§cVocê já está jogando!");
            player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 1);
            return;
        }
        int playerElo = eloManager.getElo(player.getUniqueId(), gameType.toLowerCase());
        List<PlayerQueue> queue = gameQueue.computeIfAbsent(gameType, k -> new ArrayList<>());
        queue.add(new PlayerQueue(player, playerElo));
        player.sendMessage("");
        player.sendMessage("§7Você entrou na fila do modo: §5" + gameType);
        player.sendMessage("§7Digite §5/leavequeue §7para sair da fila.");
        player.sendMessage("");
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);

        // Verifica se o modo de jogo é Ranked1v1 e chama a função de fila balanceada
        if (gameType.equalsIgnoreCase("Ranked1v1")) {
            handleRanked1v1Queue(player);
        }
    }

    private void handleRanked1v1Queue(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                List<PlayerQueue> queue = gameQueue.get("Ranked1v1");
                if (queue == null) return;
                for (PlayerQueue playerQueue : queue) {
                    if (playerQueue.getPlayer().equals(player)) {
                        searchForBalancedMatch(playerQueue);
                        return;
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 200); // Executa a cada 10 segundos (200 ticks)
    }

    public List<Player> getQueue(String gameType) {
        List<PlayerQueue> playerQueues = gameQueue.getOrDefault(gameType, new ArrayList<>());
        List<Player> players = new ArrayList<>();
        for (PlayerQueue playerQueue : playerQueues) {
            players.add(playerQueue.getPlayer());
        }
        return players;
    }

    public void leaveQueue(Player player) {
        for (List<PlayerQueue> queue : gameQueue.values()) {
            for (PlayerQueue playerQueue : queue) {
                if (playerQueue.getPlayer().equals(player)) {
                    queue.remove(playerQueue);
                    player.sendMessage("");
                    player.sendMessage("§7Você saiu da fila.");
                    player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
                    return;
                }
            }
        }
        player.sendMessage("§cVocê não está na fila.");
        player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 1);
    }

    private void startQueueTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (String gameType : gameQueue.keySet()) {
                    List<PlayerQueue> queue = new ArrayList<>(gameQueue.get(gameType));
                    for (PlayerQueue playerQueue : queue) {
                        if ((System.currentTimeMillis() - playerQueue.getJoinTime()) >= 300000) { // 5 minutos
                            leaveQueue(playerQueue.getPlayer());
                            continue;
                        }
                        searchForMatch(playerQueue, gameType);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 200); // Executa a cada 10 segundos (200 ticks)
    }

    private void searchForBalancedMatch(PlayerQueue playerQueue) {
        int baseElo = playerQueue.getElo();
        int timeInQueue = (int) ((System.currentTimeMillis() - playerQueue.getJoinTime()) / 1000);
        int range = (timeInQueue / 10) * 50;

        int minElo = Math.max(baseElo - range, 0);
        int maxElo = baseElo + range;

        List<PlayerQueue> queue = gameQueue.get("Ranked1v1");
        for (PlayerQueue otherPlayer : queue) {
            if (otherPlayer == playerQueue) continue;
            if (Math.abs(otherPlayer.getElo() - baseElo) <= range) {
                startGame(Arrays.asList(playerQueue, otherPlayer), "Ranked1v1");
                return;
            }
        }

        if (timeInQueue % 10 == 0) { // Enviar mensagem a cada 10 segundos
            playerQueue.getPlayer().sendMessage("");
            playerQueue.getPlayer().sendMessage("§c§lPulse Ranked");
            playerQueue.getPlayer().sendMessage("§fProcurando partida...");
            playerQueue.getPlayer().sendMessage("§fModo: §5Ranked 1v1");
            playerQueue.getPlayer().sendMessage("§fElo: §5[§7" + minElo + "§f-§7" + maxElo + "§5]");
            playerQueue.getPlayer().sendMessage("");
        }
    }

    private void searchForMatch(PlayerQueue playerQueue, String gameType) {
        int baseElo = playerQueue.getElo();
        int timeInQueue = (int) ((System.currentTimeMillis() - playerQueue.getJoinTime()) / 1000);
        int range = (timeInQueue / 10) * 50;

        int minElo = Math.max(baseElo - range, 0);
        int maxElo = baseElo + range;

        List<PlayerQueue> queue = gameQueue.get(gameType);
        for (PlayerQueue otherPlayer : queue) {
            if (otherPlayer == playerQueue) continue;
            if (Math.abs(otherPlayer.getElo() - baseElo) <= range) {
                startGame(Arrays.asList(playerQueue, otherPlayer), gameType);
                return;
            }
        }

        if (timeInQueue % 10 == 0) { // Enviar mensagem a cada 10 segundos
            playerQueue.getPlayer().sendMessage("");
            playerQueue.getPlayer().sendMessage("§c§lPulse Ranked");
            playerQueue.getPlayer().sendMessage("§fProcurando partida...");
            playerQueue.getPlayer().sendMessage("§fElo: §5[" + minElo + "§f~§5" + maxElo + "]");
            playerQueue.getPlayer().sendMessage("");
        }
    }

    private void startGame(List<PlayerQueue> players, String gameType) {
        for (PlayerQueue playerQueue : players) {
            leaveQueue(playerQueue.getPlayer()); // Remove da fila antes de iniciar a partida
        }

        for (PlayerQueue playerQueue : players) {
            Player player = playerQueue.getPlayer();
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
                    Bukkit.getScheduler().runTaskLater(plugin, () ->
                            bedwarsAPI.getArenaUtil().getArenaByPlayer(player).getStartingTask().setCountdown(5), 20L);
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

// Classe para armazenar dados do jogador na fila
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