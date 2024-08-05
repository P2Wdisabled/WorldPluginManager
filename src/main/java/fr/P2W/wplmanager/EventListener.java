package fr.P2W.wplmanager;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.world.WorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.command.PluginCommand;
import org.bukkit.scheduler.BukkitTask;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

public class EventListener implements Listener {
    private final WorldPluginManager plugin;

    public EventListener(WorldPluginManager plugin) {
        this.plugin = plugin;
    }

    // Handle cancellable world events
    @EventHandler
    public void onWorldEvent(WorldEvent event) {
        if (event instanceof Cancellable) {
            Cancellable cancellableEvent = (Cancellable) event;
            World world = event.getWorld();
            String worldName = world.getName();

            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                String pluginId = plugin.getDescription().getName();
                if (!this.plugin.isPluginEnabledInWorld(pluginId, worldName)) {
                    cancellableEvent.setCancelled(true);
                    return;
                }
            }
        }
    }

    // Handle player chat events
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        String senderWorld = sender.getWorld().getName();

        Iterator<Player> iterator = event.getRecipients().iterator();
        while (iterator.hasNext()) {
            Player recipient = iterator.next();
            String recipientWorld = recipient.getWorld().getName();

            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                if (!this.plugin.isPluginEnabledInWorld(plugin.getName(), senderWorld) ||
                    !this.plugin.isPluginEnabledInWorld(plugin.getName(), recipientWorld)) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    // Handle server commands
    @EventHandler
    public void onServerCommand(ServerCommandEvent event) {
        String command = event.getCommand();
        String[] parts = command.split(" ");
        String commandLabel = parts[0];

        PluginCommand pluginCommand = Bukkit.getServer().getPluginCommand(commandLabel);
        if (pluginCommand != null) {
            Plugin plugin = pluginCommand.getPlugin();
            String pluginId = plugin.getDescription().getName();
            for (Player player : Bukkit.getOnlinePlayers()) {
                String worldName = player.getWorld().getName();
                if (!this.plugin.isPluginEnabledInWorld(pluginId, worldName)) {
                    event.setCancelled(true);
                    Bukkit.getLogger().info("Command " + command + " isn't available because " + pluginId + " is not enabled in the world " + worldName);
                    break;
                }
            }
        }
    }

    // Handle plugin enabling
    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        Plugin plugin = event.getPlugin();
        String pluginId = plugin.getDescription().getName();
        for (World world : Bukkit.getWorlds()) {
            if (!this.plugin.isPluginEnabledInWorld(pluginId, world.getName())) {
                cancelPluginTasks(plugin, world);
            } else {
                reschedulePluginTasks(plugin, world);
            }
        }
    }

    // Handle player command preprocessing
    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();
        String message = event.getMessage();

        if (message.startsWith("//")) {
            handleWorldEditCommand(event, player, worldName, message.substring(2));
            return;
        }

        String commandLabel = message.substring(1).split(" ")[0];

        PluginCommand command = Bukkit.getServer().getPluginCommand(commandLabel);
        if (command != null) {
            Plugin plugin = command.getPlugin();
            String pluginId = plugin.getDescription().getName();
            if (!this.plugin.isPluginEnabledInWorld(pluginId, worldName)) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("plugin", plugin.getName());
                placeholders.put("world", worldName);
                player.sendMessage(this.plugin.getMessage("plugin_disabled", placeholders));
                event.setCancelled(true);
            }
        }
    }

    // Handle specific WorldEdit commands
    private void handleWorldEditCommand(PlayerCommandPreprocessEvent event, Player player, String worldName, String command) {
        String commandLabel = command.split(" ")[0];

        if (WorldPluginManager.WORLD_EDIT_COMMANDS.contains(commandLabel)) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldEdit");
            if (plugin != null) {
                String pluginId = plugin.getDescription().getName();
                if (!this.plugin.isPluginEnabledInWorld(pluginId, worldName)) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("plugin", plugin.getName());
                    placeholders.put("world", worldName);
                    player.sendMessage(this.plugin.getMessage("plugin_disabled", placeholders));
                    event.setCancelled(true);
                }
            }
        }
    }

    // Cancel plugin tasks related to a world
    private void cancelPluginTasks(Plugin plugin, World world) {
        Bukkit.getScheduler().getPendingTasks().stream()
            .filter(task -> task.getOwner().equals(plugin))
            .filter(task -> isTaskRelatedToWorld(task, world))
            .forEach(BukkitTask::cancel);
    }

    // Check if a task is related to a specific world
    private boolean isTaskRelatedToWorld(BukkitTask task, World world) {
        if (task.getOwner().getName().equalsIgnoreCase(world.getName())) {
            return true;
        }
        return false;
    }

    // Reschedule tasks for a plugin and world if necessary
    private void reschedulePluginTasks(Plugin plugin, World world) {
    }
}
