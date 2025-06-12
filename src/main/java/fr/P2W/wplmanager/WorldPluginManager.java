package fr.P2W.wplmanager;

import fr.P2W.wplmanager.commands.DisplCommand;
import fr.P2W.wplmanager.commands.EnplCommand;
import fr.P2W.wplmanager.commands.FlagsCommand;
import fr.P2W.wplmanager.commands.FocusCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class WorldPluginManager extends JavaPlugin implements Listener {
    private Map<String, Map<String, Boolean>> pluginWorldStatus = new HashMap<>();
    private final String PLUGIN_NAME = "WorldPluginManager";
    private FileConfiguration messagesConfig;

    private static final List<String> WORLD_EDIT_COMMANDS = Arrays.asList(
        "help", "tool", "sel", "desel", "pos1", "pos2", "hpos1", "hpos2", "chunk",
        "expand", "contract", "outset", "inset", "size", "count", "distr", "set", 
        "replace", "overlay", "naturalize", "walls", "outline", "forest", "cyl", 
        "hcyl", "sphere", "hsphere", "pyramid", "hpyramid", "line", "curve", 
        "generate", "drain", "fill", "fillr", "floodfill", "fixlava", "fixwater", 
        "removeabove", "removebelow", "removenear", "remove", "stack", "move", 
        "copy", "cut", "paste", "rotate", "flip", "schematic", "load", "save", 
        "list", "remove", "clear", "cancel", "undo", "redo", "snapshot", "list", 
        "restore", "info", "history", "sel", "wand", "drawsel", "replacenear", 
        "smooth", "deform", "generate", "forestgen", "brush", "mat", "mask", 
        "replacenear", "farwand", "tool", "snap", "thru", "unstuck", "toggleplace", 
        "tree", "craftscripts", "cs"
    );

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadMessagesConfig();
        getCommand("displ").setExecutor(new DisplCommand(this));
        getCommand("enpl").setExecutor(new EnplCommand(this));
        getCommand("flags").setExecutor(new FlagsCommand(this));
        getCommand("focus").setExecutor(new FocusCommand(this));
        getCommand("displ").setTabCompleter(new DisplCommand(this));
        getCommand("enpl").setTabCompleter(new EnplCommand(this));
        getCommand("flags").setTabCompleter(new FlagsCommand(this));
        getCommand("focus").setTabCompleter(new FocusCommand(this));
        loadConfig();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Plugin enabled!");
    }

    @Override
    public void onDisable() {
        saveConfig();
        getLogger().info("Plugin disabled!");
    }

    private void loadMessagesConfig() {
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
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
    private void loadConfig() {
        ConfigurationSection section = getConfig().getConfigurationSection("pluginWorldStatus");
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

    public void saveConfigData() {
        getConfig().set("pluginWorldStatus", pluginWorldStatus);
        saveConfig();
    }

    public String getPluginId(String pluginName) {
        Plugin plugin = getServer().getPluginManager().getPlugin(pluginName);
        return plugin != null ? plugin.getDescription().getName() : null;
    }

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

        PluginCommand command = getServer().getPluginCommand(commandLabel);
        if (command != null) {
            Plugin plugin = command.getPlugin();
            String pluginId = plugin.getDescription().getName();
            if (!isPluginEnabledInWorld(pluginId, worldName)) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("plugin", plugin.getName());
                placeholders.put("world", worldName);
                player.sendMessage(getMessage("plugin_disabled", placeholders));
                event.setCancelled(true);
            }
        }
    }

    private void handleWorldEditCommand(PlayerCommandPreprocessEvent event, Player player, String worldName, String command) {
        String commandLabel = command.split(" ")[0];

        if (WORLD_EDIT_COMMANDS.contains(commandLabel)) {
            Plugin plugin = getServer().getPluginManager().getPlugin("WorldEdit");
            if (plugin != null) {
                String pluginId = plugin.getDescription().getName();
                if (!isPluginEnabledInWorld(pluginId, worldName)) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("plugin", plugin.getName());
                    placeholders.put("world", worldName);
                    player.sendMessage(getMessage("plugin_disabled", placeholders));
                    event.setCancelled(true);
                }
            }
        }
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
        pluginWorldStatus.get(worldName).forEach((plugin, status) -> {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("plugin", plugin);
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
