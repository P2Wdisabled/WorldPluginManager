package fr.P2W.wplmanager.utils;

import fr.P2W.wplmanager.WorldPluginManager;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.P2W.wplmanager.utils.WorldManager.isPluginEnabledInWorld;
import static org.bukkit.Bukkit.getServer;

public class WorldEditManager {
    WorldPluginManager plugin;


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

    public WorldEditManager(WorldPluginManager plugin) {
        this.plugin = plugin;
        new ConfigManager(plugin);
    }


    public void handleWorldEditCommand(PlayerCommandPreprocessEvent event, Player player, String worldName, String command) {
        String commandLabel = command.split(" ")[0];

        if (WORLD_EDIT_COMMANDS.contains(commandLabel)) {
            Plugin plugin = getServer().getPluginManager().getPlugin("WorldEdit");
            if (plugin != null) {
                String pluginId = plugin.getDescription().getName();
                if (!isPluginEnabledInWorld(pluginId, worldName)) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("plugin", plugin.getName());
                    placeholders.put("world", worldName);
                    player.sendMessage(ConfigManager.getMessage("plugin_disabled", placeholders));
                    event.setCancelled(true);
                }
            }
        }
    }
}
