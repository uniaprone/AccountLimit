package org.zzq.iPAccountDetection.Listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;
import org.zzq.iPAccountDetection.Service.DetectionService;
import org.zzq.iPAccountDetection.Service.LimitService;

public class PlayerCommandListener implements Listener {
    private final LimitService limitService;

    public PlayerCommandListener(LimitService limitService) {
        this.limitService = limitService;
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        limitService.handleCommand(event);
    }
}
