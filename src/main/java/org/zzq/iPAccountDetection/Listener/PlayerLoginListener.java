package org.zzq.iPAccountDetection.Listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.zzq.iPAccountDetection.DetectionService;

public class PlayerLoginListener implements Listener {
    private final DetectionService detectionService;
    private Plugin plugin;

    public PlayerLoginListener(DetectionService detectionService, Plugin plugin) {
        this.detectionService = detectionService;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent event) {
        plugin.getLogger().info(event.getPlayer().getAddress().getAddress().getAddress().toString());
        plugin.getLogger().info(event.getPlayer().getAddress().getAddress().getHostAddress());
        detectionService.handlePlayerLogin(event.getPlayer(), event.getPlayer().getAddress().getAddress().getHostAddress());
    }
}
