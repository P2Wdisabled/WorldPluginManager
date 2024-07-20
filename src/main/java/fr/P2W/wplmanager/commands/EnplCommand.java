package fr.P2W.wplmanager.commands;

import fr.P2W.wplmanager.WorldPluginManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EnplCommand implements TabExecutor, TabCompleter {
    private final WorldPluginManager plugin;

    public EnplCommand(WorldPluginManager plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("worldpluginmanager.enpl")) {
            sender.sendMessage(plugin.getMessage("no_permission", null));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("Usage: /enpl <plugin> <world>");
            return false;
        }

        String pluginName = args[0];
        String worldName = args[1];
        World world = plugin.getServer().getWorld(worldName);

        if (world == null) {
            sender.sendMessage("The specified world does not exist.");
            return false;
        }

        plugin.setPluginStatus(pluginName, worldName, true, sender);
        return true;
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
        return null;
    }
}
