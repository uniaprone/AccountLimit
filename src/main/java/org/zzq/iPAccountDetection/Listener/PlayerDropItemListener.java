package org.zzq.iPAccountDetection.Listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.zzq.iPAccountDetection.Service.LimitService;

public class PlayerDropItemListener implements Listener {
    private final LimitService limitService;

    public PlayerDropItemListener(LimitService limitService) {
        this.limitService = limitService;
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        limitService.handlePlayerDropItem(event);
    }
}
