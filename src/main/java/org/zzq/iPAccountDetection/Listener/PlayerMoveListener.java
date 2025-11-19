package org.zzq.iPAccountDetection.Listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.zzq.iPAccountDetection.Service.DetectionService;
import org.zzq.iPAccountDetection.Service.LimitDataService;
import org.zzq.iPAccountDetection.Service.LimitService;

public class PlayerMoveListener implements Listener {
    private final LimitService limitService;

    public PlayerMoveListener(LimitService limitService) {
        this.limitService = limitService;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        limitService.handleMovement(event);
    }
}
