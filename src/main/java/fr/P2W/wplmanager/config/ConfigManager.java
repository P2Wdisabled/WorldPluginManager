package fr.P2W.wplmanager.config;

import fr.P2W.wplmanager.WorldPluginManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles configuration and message management for WorldPluginManager.
 */
public class ConfigManager {
    private final WorldPluginManager plugin;
    private Map<String, Map<String, Boolean>> pluginWorldStatus = new HashMap<>();
    private FileConfiguration messagesConfig;

    public ConfigManager(WorldPluginManager plugin) {
        this.plugin = plugin;
    }

    public void loadMessagesConfig() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        String message = messagesConfig.getString(key, key);
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    @SuppressWarnings("unchecked")
    public void loadConfig() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("pluginWorldStatus");
        if (section != null) {
            Set<String> worlds = section.getKeys(false);
            for (String world : worlds) {
                ConfigurationSection worldSection = section.getConfigurationSection(world);
                if (worldSection != null) {
                    Map<String, Boolean> plugins = new HashMap<>();
                    for (String p : worldSection.getKeys(false)) {
                        plugins.put(p, worldSection.getBoolean(p));
                    }
                    pluginWorldStatus.put(world, plugins);
                }
            }
        }
    }

    public void saveConfigData() {
        plugin.getConfig().set("pluginWorldStatus", pluginWorldStatus);
        plugin.saveConfig();
    }

    public String getPluginId(String pluginName) {
        org.bukkit.plugin.Plugin p = plugin.getServer().getPluginManager().getPlugin(pluginName);
        return p != null ? p.getDescription().getName() : null;
    }

    public boolean isPluginEnabledInWorld(String pluginId, String worldName) {
        return !pluginWorldStatus.containsKey(worldName) ||
               pluginWorldStatus.get(worldName).getOrDefault(pluginId, true);
    }

    public void setPluginStatus(String pluginName, String worldName, boolean status, CommandSender sender) {
        String pluginId = getPluginId(pluginName);
        if (pluginId == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("plugin", pluginName);
            sender.sendMessage(getMessage("plugin_not_found", placeholders));
            return;
        }

        if (pluginName.equalsIgnoreCase(plugin.getPluginName()) && !status) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("plugin", plugin.getPluginName());
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

    public void displayPluginFlag(String worldName, String pluginName, CommandSender sender) {
        String pluginId = getPluginId(pluginName);
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
        pluginWorldStatus.get(worldName).forEach((pluginName, status) -> {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("plugin", pluginName);
            placeholders.put("world", worldName);
            sender.sendMessage(getMessage(status ? "plugin_enabled" : "plugin_disabled", placeholders));
        });
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
