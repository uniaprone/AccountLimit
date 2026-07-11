package org.zzq.iPAccountDetection.Service;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.jetbrains.annotations.NotNull;
import org.zzq.iPAccountDetection.infrastructure.*;
import org.zzq.iPAccountDetection.model.aggregate.Group;
import org.zzq.iPAccountDetection.model.repository.ILimitedAccountRepository;
import org.zzq.iPAccountDetection.model.repository.IMemoryGroupRepository;
import org.zzq.iPAccountDetection.util.ColorUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LimitService implements ConfigChangeListener {
    private ILimitedAccountRepository limitAccountRepository;
    private IMemoryGroupRepository memoryGroupRepository;
    private ConfigManager configManager;
    private LimitEventConfig limitEventConfig;
    private LimitTitleConfig limitTitleConfig;
    private LimitMessageConfig limitMessageConfig;
    private Title limitTitle;
    private List<String> limitMessage;
    private Map<String, Integer> playerLimitTriggerTimes = new HashMap<>();
    public LimitService(ILimitedAccountRepository limitAccountRepository, IMemoryGroupRepository iMemoryGroupRepository, ConfigManager configManager){
        this.limitAccountRepository = limitAccountRepository;
        this.memoryGroupRepository = iMemoryGroupRepository;
        this.configManager = configManager;
        limitEventConfig = configManager.getLimitDisplayConfig();
        limitTitleConfig = limitEventConfig.getTitleConfig();
        limitMessageConfig = limitEventConfig.getMessageConfig();
        initLimitMessage();
        configManager.registerListener(this);
    }

    public void handleMovement(PlayerMoveEvent event){
        if(!limitEventConfig.isEnable()) return;
        if(event == null) return;
        Player player = event.getPlayer();
        if(isLimit(player)){
            event.setCancelled(true);
            limitMessage(player);
        }
    }

    public void handleOpenInventory(InventoryOpenEvent event){
        if(!limitEventConfig.isEnable()) return;
        if(event == null) return;
        Player player = (Player) event.getPlayer();
        if(isLimit(player)){
            event.setCancelled(true);
            limitMessage(player);
        }
    }

    public void handleInventoryClick(InventoryClickEvent event){
        if(!limitEventConfig.isEnable()) return;
        if(event == null) return;
        Player player = (Player) event.getWhoClicked();
        if(isLimit(player)){
            event.setCancelled(true);
            limitMessage(player);
        }
    }

    public void handlePlayerDropItem(PlayerDropItemEvent event){
        if(!limitEventConfig.isEnable()) return;
        if(event == null) return;
        Player player = event.getPlayer();
        if(isLimit(player)){
            event.setCancelled(true);
            limitMessage(player);
        }
    }

    public void handleInteract(PlayerInteractEvent event){
        if(!limitEventConfig.isEnable()) return;
        if(event == null) return;
        Player player = event.getPlayer();
        if(isLimit(player)){
            event.setCancelled(true);
            limitMessage(player);
        }
    }

    public void handleCommand(PlayerCommandPreprocessEvent event){
        if(!limitEventConfig.isEnable()) return;
        if(event == null) return;
        Player player = event.getPlayer();
        if(isLimit(player)){
            event.setCancelled(true);
            limitMessage(player);
        }
    }

    public void handleQuit(PlayerQuitEvent event){
        if(!limitEventConfig.isEnable()) return;
        if(event == null) return;
        Player player = event.getPlayer();
        if(limitAccountRepository.isLimitedAccount(player.getUniqueId().toString()) && !player.hasPermission("")){
            limitAccountRepository.removeLimitedAccount(player.getUniqueId().toString());
        }
        playerLimitTriggerTimes.remove(player.getUniqueId().toString());
    }

    public void handleMessage(AsyncChatEvent event){
        if(!limitEventConfig.isEnable()) return;
        if(event == null) return;
        Player player = event.getPlayer();
        if(isLimit(player)){
            event.setCancelled(true);
            limitMessage(player);
        }
    }

    private void limitMessage(Player player){
        String playerId = player.getUniqueId().toString();
        int times = playerLimitTriggerTimes.getOrDefault(player.getUniqueId().toString(), limitEventConfig.getTriggerIntervalTimes());
        if(times >= limitEventConfig.getTriggerIntervalTimes()){
            if(limitTitleConfig.isEnable()){
                player.showTitle(limitTitle);
            }
            if(limitMessageConfig.isEnable()){
                for (String message : limitMessage){
                    player.sendMessage(message);
                }
            }
            playerLimitTriggerTimes.put(playerId, 0);
        }else{
            playerLimitTriggerTimes.put(playerId, times + 1);
        }

    }

    @Override
    public void onConfigChanged(@NotNull ConfigManager configManager) {
        initLimitMessage();
    }

    private void initLimitMessage(){
        Component firstMessage = Component.text(limitTitleConfig.getTitleMessage())
                .color(ColorUtil.convertColor(limitTitleConfig.getTitleColor()))
                .decorate(TextDecoration.BOLD);
        Component secondMessage = Component.text(limitTitleConfig.getSubTitleMessage())
                .color(NamedTextColor.YELLOW)
                .decorate(TextDecoration.BOLD);
        limitTitle = Title.title(firstMessage, 
                secondMessage, 
                limitTitleConfig.getFadeInTicks(),
                limitTitleConfig.getStayTicks(),
                limitTitleConfig.getFadeOutTicks());
        
        limitMessage = limitMessageConfig.getMessages();
    }

    private boolean isLimit(Player player){
        String playerId = player.getUniqueId().toString();
        boolean isLimitAccount = limitAccountRepository.isLimitedAccount(playerId);
        Group group = memoryGroupRepository.getGroupByAccountId(playerId);
        if(group == null) return true;
        boolean isBan = group.isBan();
        return isLimitAccount || isBan;
    }
}
