package fr.P2W.wplmanager.utils;

import fr.P2W.wplmanager.WorldPluginManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static fr.P2W.wplmanager.utils.ConfigManager.*;
import static org.bukkit.Bukkit.getServer;

public class PluginManager {
    private final String PLUGIN_NAME = "WorldPluginManager";
    WorldPluginManager plugin;

    public PluginManager(WorldPluginManager plugin) {
        this.plugin = plugin;
    }

    public String getPluginId(String pluginName) {
        Plugin plugin = getServer().getPluginManager().getPlugin(pluginName);
        return plugin != null ? plugin.getDescription().getName() : null;
    }

    public void setPluginStatus(String pluginName, String worldName, boolean status, CommandSender sender) {
        String pluginId = getPluginId(pluginName);
        if (pluginId == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("plugin", pluginName);
            sender.sendMessage(getMessage("plugin_not_found", placeholders));
            return;
        }

        if (pluginName.equalsIgnoreCase(PLUGIN_NAME) && !status) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("plugin", PLUGIN_NAME);
            sender.sendMessage(getMessage("cannot_disable_main_plugin", placeholders));
            return;
        }

        pluginWorldStatus.putIfAbsent(worldName, new HashMap<>());
        pluginWorldStatus.get(worldName).put(pluginId, status);
        saveConfigData();
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("plugin", pluginName);
        placeholders.put("world", worldName);
        sender.sendMessage(getMessage(status ? "plugin_enabled" : "plugin_disabled", placeholders));
    }

    public void setFocus(String pluginName, String worldName, CommandSender sender) {
        String pluginId = getPluginId(pluginName);
        if (pluginId == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("plugin", pluginName);
            sender.sendMessage(getMessage("plugin_not_found", placeholders));
            return;
        }

        for (String world : Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList())) {
            setPluginStatus(pluginName, world, world.equals(worldName), sender);
        }
        saveConfigData();
    }
}
