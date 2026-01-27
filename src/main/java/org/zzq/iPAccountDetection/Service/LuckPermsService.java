package org.zzq.iPAccountDetection.Service;

import org.bukkit.entity.Player;
import org.zzq.iPAccountDetection.infrastructure.ConfigChangeListener;
import org.zzq.iPAccountDetection.infrastructure.ConfigManager;
import org.zzq.iPAccountDetection.infrastructure.LogUtil;
import org.zzq.iPAccountDetection.model.ILuckpermsExpansion;
import org.zzq.iPAccountDetection.model.aggregate.Group;
import org.zzq.iPAccountDetection.model.repository.IMemoryGroupRepository;

public class LuckPermsService implements ConfigChangeListener {
    private IMemoryGroupRepository memoryGroupRepository;
    private ILuckpermsExpansion luckpermsExpansion;
    private LogUtil logUtil;
    private boolean isLuckPermsEnable;
    private String luckpermsSecondaryAccountGroupName;

    public LuckPermsService(IMemoryGroupRepository memoryGroupRepository, ILuckpermsExpansion luckpermsExpansion, ConfigManager configManager, LogUtil logUtil) {
        this.memoryGroupRepository = memoryGroupRepository;
        this.luckpermsExpansion = luckpermsExpansion;
        this.logUtil = logUtil;
        this.isLuckPermsEnable = configManager.isLuckPermsEnable();
        this.luckpermsSecondaryAccountGroupName = configManager.getLuckPermsSecondaryAccountGroupName();
        configManager.registerListener(this);
    }

    public void handleLoginEvent(Player player){
        if(!isLuckPermsEnable) return;
        String playerId = player.getUniqueId().toString();
        Group group = memoryGroupRepository.getGroupByAccountId(playerId);
        if(group == null) return;
        logUtil.info(player.getName());
        if(group.isMainAccount(playerId)){
            if(luckpermsExpansion.isPlayerInGroup(playerId, luckpermsSecondaryAccountGroupName)){
                luckpermsExpansion.removePlayerFromGroup(playerId, luckpermsSecondaryAccountGroupName);
            }
        }else{
            if(!luckpermsExpansion.isPlayerInGroup(playerId, luckpermsSecondaryAccountGroupName)){
                luckpermsExpansion.addPlayerToGroup(playerId, luckpermsSecondaryAccountGroupName);
            }
        }

    }

    @Override
    public void onConfigChanged(ConfigManager configManager) {
        this.isLuckPermsEnable = configManager.isLuckPermsEnable();
        this.luckpermsSecondaryAccountGroupName = configManager.getLuckPermsSecondaryAccountGroupName();
    }
}
