package org.zzq.iPAccountDetection.Listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.zzq.iPAccountDetection.Service.LimitService;

public class PlayerChatListener implements Listener {
    private final LimitService limitService;

    public PlayerChatListener(LimitService limitService) {
        this.limitService = limitService;
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        limitService.handleMessage(event);
    }
}
