package fr.P2W.wplmanager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public class WorldPluginManager extends JavaPlugin implements TabExecutor, Listener {
    private Map<String, Map<String, Boolean>> pluginWorldStatus = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.getCommand("displ").setExecutor(this);
        this.getCommand("enpl").setExecutor(this);
        this.getCommand("flags").setExecutor(this);
        this.getCommand("focus").setExecutor(this);
        this.getCommand("displ").setTabCompleter(this);
        this.getCommand("enpl").setTabCompleter(this);
        this.getCommand("flags").setTabCompleter(this);
        this.getCommand("focus").setTabCompleter(this);
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

    private void saveConfigData() {
        getConfig().set("pluginWorldStatus", pluginWorldStatus);
        saveConfig();
    }

    private String getPluginId(String pluginName) {
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

    private boolean isPluginEnabledInWorld(String pluginId, String worldName) {
        return !pluginWorldStatus.containsKey(worldName) || 
               pluginWorldStatus.get(worldName).getOrDefault(pluginId, true);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <plugin> <world>");
            return false;
        }

        String pluginName = args[0];
        String worldName = args[1];
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            sender.sendMessage(ChatColor.RED + "Le monde spécifié n'existe pas.");
            return false;
        }

        switch (label.toLowerCase()) {
            case "displ":
                setPluginStatus(pluginName, worldName, false, sender);
                break;
            case "enpl":
                setPluginStatus(pluginName, worldName, true, sender);
                break;
            case "flags":
                displayFlags(worldName, sender);
                break;
            case "focus":
                setFocus(pluginName, worldName, sender);
                break;
            default:
                return false;
        }

        saveConfigData();
        return true;
    }

    private void setPluginStatus(String pluginName, String worldName, boolean status, CommandSender sender) {
        String pluginId = getPluginId(pluginName);
        if (pluginId == null) {
            sender.sendMessage(ChatColor.RED + "Plugin " + pluginName + " non trouvé.");
            return;
        }

        pluginWorldStatus.putIfAbsent(worldName, new HashMap<>());
        pluginWorldStatus.get(worldName).put(pluginId, status);
        sender.sendMessage(ChatColor.GREEN + "Plugin " + pluginName + " (" + pluginId + ") " + (status ? "activé" : "désactivé") + " dans le monde " + worldName);
    }

    private void displayFlags(String worldName, CommandSender sender) {
        if (!pluginWorldStatus.containsKey(worldName)) {
            sender.sendMessage(ChatColor.YELLOW + "Aucun plugin configuré pour le monde " + worldName);
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "Plugins pour le monde " + worldName + ":");
        pluginWorldStatus.get(worldName).forEach((plugin, status) ->
                sender.sendMessage(ChatColor.AQUA + plugin + ": " + (status ? "Activé" : "Désactivé")));
    }

    private void setFocus(String pluginName, String worldName, CommandSender sender) {
        String pluginId = getPluginId(pluginName);
        if (pluginId == null) {
            sender.sendMessage(ChatColor.RED + "Plugin " + pluginName + " non trouvé.");
            return;
        }

        for (String world : Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList())) {
            setPluginStatus(pluginName, world, world.equals(worldName), sender);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.stream(Bukkit.getPluginManager().getPlugins())
                         .map(Plugin::getName)
                         .collect(Collectors.toList());
        } else if (args.length == 2) {
            return Bukkit.getWorlds().stream()
                         .map(World::getName)
                         .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
