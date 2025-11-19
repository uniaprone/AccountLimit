package org.zzq.iPAccountDetection.Listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.zzq.iPAccountDetection.Service.LimitService;

public class PlayerQuitListener implements Listener {
    private final LimitService limitService;

    public PlayerQuitListener(LimitService limitService) {
        this.limitService = limitService;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        limitService.handleQuit(event);
    }
}
