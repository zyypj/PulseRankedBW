package br.com.pulse.ranked.queue;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveQueueCommand implements CommandExecutor {

    private final QueueManager queueManager;

    public LeaveQueueCommand(QueueManager queueManager) {
        this.queueManager = queueManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser executado por jogadores.");
            return true;
        }
        Player player = (Player) sender;
        queueManager.leaveQueue(player);
        return true;
    }
}

