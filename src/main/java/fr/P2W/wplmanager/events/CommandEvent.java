package fr.P2W.wplmanager.events;

import fr.P2W.wplmanager.WorldPluginManager;
import fr.P2W.wplmanager.utils.ConfigManager;
import fr.P2W.wplmanager.utils.WorldEditManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

import static fr.P2W.wplmanager.utils.WorldManager.isPluginEnabledInWorld;
import static org.bukkit.Bukkit.getServer;

public class CommandEvent implements Listener {
    WorldPluginManager plugin;
    WorldEditManager worldEditManager;
    public CommandEvent(WorldPluginManager plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();
        String message = event.getMessage();

        if (message.startsWith("//")) {
            worldEditManager.handleWorldEditCommand(event, player, worldName, message.substring(2));
            return;
        }

        String commandLabel = message.substring(1).split(" ")[0];

        PluginCommand command = getServer().getPluginCommand(commandLabel);
        if (command != null) {
            Plugin plugin = command.getPlugin();
            String pluginId = plugin.getDescription().getName();
            if (!isPluginEnabledInWorld(pluginId, worldName)) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("plugin", plugin.getName());
                placeholders.put("world", worldName);
                player.sendMessage(ConfigManager.getMessage("plugin_disabled", placeholders));
                event.setCancelled(true);
            }
        }
    }
}
