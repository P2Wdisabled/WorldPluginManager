package fr.P2W.wplmanager;

import fr.P2W.wplmanager.commands.DisplCommand;
import fr.P2W.wplmanager.commands.EnplCommand;
import fr.P2W.wplmanager.commands.FlagsCommand;
import fr.P2W.wplmanager.commands.FocusCommand;
import fr.P2W.wplmanager.events.CommandEvent;
import fr.P2W.wplmanager.utils.*;
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


    @Override
    public void onEnable() {
        saveDefaultConfig();
        ConfigManager.loadMessagesConfig();
        ConfigManager.loadConfig();

        getCommand("displ").setExecutor(new DisplCommand(this));
        getCommand("enpl").setExecutor(new EnplCommand(this));
        getCommand("flags").setExecutor(new FlagsCommand(this));
        getCommand("focus").setExecutor(new FocusCommand(this));
        getCommand("displ").setTabCompleter(new DisplCommand(this));
        getCommand("enpl").setTabCompleter(new EnplCommand(this));
        getCommand("flags").setTabCompleter(new FlagsCommand(this));
        getCommand("focus").setTabCompleter(new FocusCommand(this));

        getServer().getPluginManager().registerEvents(new CommandEvent(this), this);

        new ConfigManager(this);
        new WorldEditManager(this);
        new FlagManager(this);
        new PluginManager(this);

        getLogger().info("Plugin enabled!");
    }

    @Override
    public void onDisable() {
        saveConfig();
        getLogger().info("Plugin disabled!");
    }

}
