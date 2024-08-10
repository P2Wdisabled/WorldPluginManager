package fr.P2W.wplmanager.utils;

import fr.P2W.wplmanager.WorldPluginManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

import static fr.P2W.wplmanager.utils.ConfigManager.getMessage;
import static fr.P2W.wplmanager.utils.ConfigManager.pluginWorldStatus;

public class FlagManager {
    private WorldPluginManager plugin;
    PluginManager pluginManager;

    public FlagManager(WorldPluginManager plugin) {
        this.plugin = plugin;
    }

    public void displayPluginFlag(String worldName, String pluginName, CommandSender sender) {
        String pluginId = pluginManager.getPluginId(pluginName);
        if (pluginId == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("plugin", pluginName);
            sender.sendMessage(getMessage("plugin_not_found", placeholders));
            return;
        }

        boolean status = pluginWorldStatus.containsKey(worldName) && pluginWorldStatus.get(worldName).getOrDefault(pluginId, true);
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("plugin", pluginName);
        placeholders.put("world", worldName);
        sender.sendMessage(getMessage(status ? "plugin_enabled" : "plugin_disabled", placeholders));
    }

    public void displayFlags(String worldName, CommandSender sender) {
        if (!pluginWorldStatus.containsKey(worldName)) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("world", worldName);
            sender.sendMessage(getMessage("world_not_found", placeholders));
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "Plugins for world " + worldName + ":");
        pluginWorldStatus.get(worldName).forEach((plugin, status) -> {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("plugin", plugin);
            placeholders.put("world", worldName);
            sender.sendMessage(getMessage(status ? "plugin_enabled" : "plugin_disabled", placeholders));
        });
    }
}
