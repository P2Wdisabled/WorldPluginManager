package fr.P2W.wplmanager.utils;

import fr.P2W.wplmanager.WorldPluginManager;
import static fr.P2W.wplmanager.utils.ConfigManager.pluginWorldStatus;

public class WorldManager {


    public static boolean isPluginEnabledInWorld(String pluginId, String worldName) {
        return !pluginWorldStatus.containsKey(worldName) ||
                pluginWorldStatus.get(worldName).getOrDefault(pluginId, true);
    }
}
