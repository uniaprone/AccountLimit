package org.zzq.iPAccountDetection.Listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.zzq.iPAccountDetection.Service.LimitService;

public class PlayerOpenInventoryEvent implements Listener {
    private LimitService limitService;

    public PlayerOpenInventoryEvent(LimitService limitService) {
        this.limitService = limitService;
    }

    @EventHandler
    public void onInventoryOpening(InventoryOpenEvent event){
        limitService.handleOpenInventory(event);
    }
}
