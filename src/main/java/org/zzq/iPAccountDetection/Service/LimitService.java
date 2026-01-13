package org.zzq.iPAccountDetection.Service;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.zzq.iPAccountDetection.infrastructure.ConfigChangeListener;
import org.zzq.iPAccountDetection.infrastructure.ConfigManager;
import org.zzq.iPAccountDetection.model.repository.ILimitedAccountRepository;

public class LimitService implements ConfigChangeListener {
    private ILimitedAccountRepository limitAccountRepository;
    private ConfigManager configManager;
    private boolean isLimitEnable;
    public LimitService(ILimitedAccountRepository limitAccountRepository, ConfigManager configManager){
        this.limitAccountRepository = limitAccountRepository;
        this.configManager = configManager;
        this.isLimitEnable = configManager.isLimitEnable();
        configManager.registerListener(this);
    }

    public void handleMovement(PlayerMoveEvent event){
        if(!isLimitEnable) return;
        if(event == null) return;
        Player player = event.getPlayer();
        if(limitAccountRepository.isLimitedAccount(player.getUniqueId().toString()) && !player.hasPermission("")){
            event.setCancelled(true);
            limitMessage(player);
        }
    }

    public void handleCommand(PlayerCommandPreprocessEvent event){
        if(!isLimitEnable) return;
        if(event == null) return;
        Player player = event.getPlayer();
        if(limitAccountRepository.isLimitedAccount(player.getUniqueId().toString()) && !player.hasPermission("")){
            event.setCancelled(true);
            limitMessage(player);
        }
    }

    public void handleQuit(PlayerQuitEvent event){
        if(!isLimitEnable) return;
        if(event == null) return;
        Player player = event.getPlayer();
        if(limitAccountRepository.isLimitedAccount(player.getUniqueId().toString()) && !player.hasPermission("")){
            limitAccountRepository.removeLimitedAccount(player.getUniqueId().toString());
        }
    }

    public void handleMessage(AsyncChatEvent event){
        if(!isLimitEnable) return;
        if(event == null) return;
        Player player = event.getPlayer();
        if(limitAccountRepository.isLimitedAccount(player.getUniqueId().toString()) && !player.hasPermission("")){
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

    @Override
    public void onConfigChanged(ConfigManager configManager) {
        this.isLimitEnable = configManager.isLimitEnable();
    }
}
