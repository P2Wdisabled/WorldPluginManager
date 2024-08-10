package fr.P2W.wplmanager.utils;

import fr.P2W.wplmanager.WorldPluginManager;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ConfigManager {
    static WorldPluginManager plugin;
    private static FileConfiguration messagesConfig;
    public static final Map<String, Map<String, Boolean>> pluginWorldStatus = new HashMap<>();


    public ConfigManager(WorldPluginManager plugin) {
        this.plugin = plugin;
    }

    public static void loadMessagesConfig() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public static String getMessage(String key, Map<String, String> placeholders) {
        String message = messagesConfig.getString(key, key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    @SuppressWarnings("unchecked")
    public static void loadConfig() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("pluginWorldStatus");
        if (section != null) {
            Set<String> worlds = section.getKeys(false);
            for (String world : worlds) {
                ConfigurationSection worldSection = section.getConfigurationSection(world);
                if (worldSection != null) {
                    Map<String, Boolean> plugins = new HashMap<>();
                    for (String plugin : worldSection.getKeys(false)) {
                        plugins.put(plugin, worldSection.getBoolean(plugin));
                    }
                    pluginWorldStatus.put(world, plugins);
                }
            }
        }
    }

    public static void saveConfigData() {
        plugin.getConfig().set("pluginWorldStatus", pluginWorldStatus);
        plugin.saveConfig();
    }
}
