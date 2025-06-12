package fr.P2W.wplmanager.packet;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketType;
import com.comphenix.protocol.wrappers.MinecraftKey;
import fr.P2W.wplmanager.WorldPluginManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Handles ProtocolLib packet listeners to block plugin channels when a plugin
 * is disabled in a world.
 */
public class PacketManager {
    private final WorldPluginManager plugin;
    private ProtocolManager protocolManager;

    public PacketManager(WorldPluginManager plugin) {
        this.plugin = plugin;
    }

    public void registerPacketListeners() {
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") == null) {
            plugin.getLogger().warning("ProtocolLib not found. Packet listeners disabled.");
            return;
        }

        protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL,
                PacketType.Play.Client.CUSTOM_PAYLOAD, PacketType.Play.Server.CUSTOM_PAYLOAD) {

            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (shouldCancelPacket(event.getPlayer(), event.getPacket().getMinecraftKeys().read(0))) {
                    event.setCancelled(true);
                }
            }

            @Override
            public void onPacketSending(PacketEvent event) {
                if (shouldCancelPacket(event.getPlayer(), event.getPacket().getMinecraftKeys().read(0))) {
                    event.setCancelled(true);
                }
            }
        });
    }

    private boolean shouldCancelPacket(Player player, MinecraftKey channel) {
        String key = channel.getFullKey();
        String pluginNamePart = key.contains(":") ? key.substring(0, key.indexOf(':')) : key;
        Plugin target = Bukkit.getPluginManager().getPlugin(pluginNamePart);
        if (target == null) {
            return false;
        }
        String pluginId = target.getDescription().getName();
        String worldName = player.getWorld().getName();
        return !plugin.isPluginEnabledInWorld(pluginId, worldName);
    }
}
