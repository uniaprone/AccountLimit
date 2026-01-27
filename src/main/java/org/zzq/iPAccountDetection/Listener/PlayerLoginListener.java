package org.zzq.iPAccountDetection.Listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.zzq.iPAccountDetection.Service.DetectionService;
import org.zzq.iPAccountDetection.Service.LuckPermsService;

public class PlayerLoginListener implements Listener {
    private final DetectionService detectionService;
    private LuckPermsService luckPermsService;

    public PlayerLoginListener(DetectionService detectionService, LuckPermsService luckPermsService) {
        this.detectionService = detectionService;
        this.luckPermsService = luckPermsService;
    }

    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent event) {
        detectionService.handlePlayerLogin(event.getPlayer(), event.getPlayer().getAddress().getAddress().getHostAddress());
        if(luckPermsService == null) return;
        luckPermsService.handleLoginEvent(event.getPlayer());
    }
}
