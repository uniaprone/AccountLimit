package org.zzq.iPAccountDetection.Listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.zzq.iPAccountDetection.Service.LimitService;

public class InventroyClickListener implements Listener {
    private final LimitService limitService;

    public InventroyClickListener(LimitService limitService) {
        this.limitService = limitService;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        limitService.handleInventoryClick(event);
    }
}
