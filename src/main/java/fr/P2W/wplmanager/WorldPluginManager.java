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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public class WorldPluginManager extends JavaPlugin implements Listener {
    private Map<String, Map<String, Boolean>> pluginWorldStatus = new HashMap<>();
    private final String PLUGIN_NAME = "WorldPluginManager";

    @Override
    public void onEnable() {
        saveDefaultConfig();
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
        getLogger().info("Plugin activé !");
    }

    @Override
    public void onDisable() {
        saveConfig();
        getLogger().info("Plugin désactivé !");
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
        String[] commandParts = event.getMessage().substring(1).split(" ");
        String commandLabel = commandParts[0];

        PluginCommand command = getServer().getPluginCommand(commandLabel);
        if (command != null) {
            Plugin plugin = command.getPlugin();
            String pluginId = plugin.getDescription().getName();
            if (!isPluginEnabledInWorld(pluginId, worldName)) {
                player.sendMessage(ChatColor.RED + "Le plugin " + plugin.getName() + " est désactivé dans ce monde.");
                event.setCancelled(true);
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
            sender.sendMessage(ChatColor.RED + "Plugin " + pluginName + " non trouvé.");
            return;
        }

        if (pluginName.equalsIgnoreCase(PLUGIN_NAME) && !status) {
            sender.sendMessage(ChatColor.RED + "Vous ne pouvez pas désactiver le plugin " + PLUGIN_NAME + " dans aucun monde.");
            return;
        }

        pluginWorldStatus.putIfAbsent(worldName, new HashMap<>());
        pluginWorldStatus.get(worldName).put(pluginId, status);
        sender.sendMessage(ChatColor.GREEN + "Plugin " + pluginName + " (" + pluginId + ") " + (status ? "activé" : "désactivé") + " dans le monde " + worldName);
    }

    public void displayPluginFlag(String worldName, String pluginName, CommandSender sender) {
        String pluginId = getPluginId(pluginName);
        if (pluginId == null) {
            sender.sendMessage(ChatColor.RED + "Plugin " + pluginName + " non trouvé.");
            return;
        }

        boolean status = pluginWorldStatus.containsKey(worldName) && pluginWorldStatus.get(worldName).getOrDefault(pluginId, true);
        sender.sendMessage(ChatColor.GOLD + "Plugin " + pluginName + " (" + pluginId + ") dans le monde " + worldName + ": " + (status ? "Activé" : "Désactivé"));
    }

    public void displayFlags(String worldName, CommandSender sender) {
        if (!pluginWorldStatus.containsKey(worldName)) {
            sender.sendMessage(ChatColor.YELLOW + "Aucun plugin configuré pour le monde " + worldName);
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "Plugins pour le monde " + worldName + ":");
        pluginWorldStatus.get(worldName).forEach((plugin, status) ->
                sender.sendMessage(ChatColor.AQUA + plugin + ": " + (status ? "Activé" : "Désactivé")));
    }

    public void setFocus(String pluginName, String worldName, CommandSender sender) {
        String pluginId = getPluginId(pluginName);
        if (pluginId == null) {
            sender.sendMessage(ChatColor.RED + "Plugin " + pluginName + " non trouvé.");
            return;
        }

        for (String world : Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList())) {
            setPluginStatus(pluginName, world, world.equals(worldName), sender);
        }
    }
}
