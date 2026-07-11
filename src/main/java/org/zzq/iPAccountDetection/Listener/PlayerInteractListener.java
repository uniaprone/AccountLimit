package org.zzq.iPAccountDetection.Listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.zzq.iPAccountDetection.Service.LimitService;

public class PlayerInteractListener implements Listener {
    private final LimitService limitService;

    public PlayerInteractListener(LimitService limitService) {
        this.limitService = limitService;
    }

    @EventHandler
    public void onPlayerMove(PlayerInteractEvent event) {
        limitService.handleInteract(event);
    }
}
