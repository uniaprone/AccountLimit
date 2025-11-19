package org.zzq.iPAccountDetection.Service;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class LimitService {
    private LimitDataService limitDataService;
    public LimitService(LimitDataService limitDataService){
        this.limitDataService = limitDataService;
    }

    public void handleMovement(PlayerMoveEvent event){
        if(event == null) return;
        Player player = event.getPlayer();
        if(limitDataService.isLimitAccount(player.getName()) && !player.hasPermission("")){
            event.setCancelled(true);
            limitMessage(player);
        }
    }

    public void handleCommand(PlayerCommandPreprocessEvent event){
        if(event == null) return;
        Player player = event.getPlayer();
        if(limitDataService.isLimitAccount(player.getName())){
            event.setCancelled(true);
            limitMessage(player);
        }
    }

    public void handleQuit(PlayerQuitEvent event){
        if(event == null) return;
        Player player = event.getPlayer();
        if(limitDataService.isLimitAccount(player.getName())){
            limitDataService.removeLimitAccount(player.getName());
        }
    }

    public void handleMessage(AsyncChatEvent event){
        if(event == null) return;
        Player player = event.getPlayer();
        if(limitDataService.isLimitAccount(player.getName())){
            event.setCancelled(true);
            limitMessage(player);
        }
    }

    private void limitMessage(Player player){
        Component firstMessage = Component.text("你的账号数量")
                .color(NamedTextColor.YELLOW)
                .decorate(TextDecoration.BOLD)
                .append(Component.text("已达上限!")
                .color(NamedTextColor.RED)
                .decorate(TextDecoration.BOLD));
        player.sendMessage(firstMessage);
        Component secondMessage = Component.text("如有疑问请联系管理员!")
                .color(NamedTextColor.YELLOW)
                .decorate(TextDecoration.BOLD);
        player.sendMessage(secondMessage);
    }
}
