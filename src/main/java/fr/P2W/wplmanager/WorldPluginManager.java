package fr.P2W.wplmanager;

import fr.P2W.wplmanager.commands.DisplCommand;
import fr.P2W.wplmanager.commands.EnplCommand;
import fr.P2W.wplmanager.commands.FlagsCommand;
import fr.P2W.wplmanager.commands.FocusCommand;
import fr.P2W.wplmanager.config.ConfigManager;
import fr.P2W.wplmanager.packet.PacketManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Main plugin class for WorldPluginManager.
 */
public class WorldPluginManager extends JavaPlugin implements Listener {
    private ConfigManager configManager;
    private PacketManager packetManager;
    private final String PLUGIN_NAME = "WorldPluginManager";

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
        configManager = new ConfigManager(this);
        configManager.loadMessagesConfig();

        getCommand("displ").setExecutor(new DisplCommand(this));
        getCommand("enpl").setExecutor(new EnplCommand(this));
        getCommand("flags").setExecutor(new FlagsCommand(this));
        getCommand("focus").setExecutor(new FocusCommand(this));
        getCommand("displ").setTabCompleter(new DisplCommand(this));
        getCommand("enpl").setTabCompleter(new EnplCommand(this));
        getCommand("flags").setTabCompleter(new FlagsCommand(this));
        getCommand("focus").setTabCompleter(new FocusCommand(this));

        configManager.loadConfig();
        getServer().getPluginManager().registerEvents(this, this);
        packetManager = new PacketManager(this);
        packetManager.registerPacketListeners();
        getLogger().info("Plugin enabled!");
    }

    @Override
    public void onDisable() {
        configManager.saveConfigData();
        getLogger().info("Plugin disabled!");
    }

    public String getPluginName() {
        return PLUGIN_NAME;
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        return configManager.getMessage(key, placeholders);
    }

    public String getPluginId(String pluginName) {
        return configManager.getPluginId(pluginName);
    }

    public boolean isPluginEnabledInWorld(String pluginId, String worldName) {
        return configManager.isPluginEnabledInWorld(pluginId, worldName);
    }

    public void setPluginStatus(String pluginName, String worldName, boolean status, CommandSender sender) {
        configManager.setPluginStatus(pluginName, worldName, status, sender);
    }

    public void displayPluginFlag(String worldName, String pluginName, CommandSender sender) {
        configManager.displayPluginFlag(worldName, pluginName, sender);
    }

    public void displayFlags(String worldName, CommandSender sender) {
        configManager.displayFlags(worldName, sender);
    }

    public void setFocus(String pluginName, String worldName, CommandSender sender) {
        configManager.setFocus(pluginName, worldName, sender);
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
}
