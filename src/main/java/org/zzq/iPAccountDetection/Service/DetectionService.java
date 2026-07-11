package org.zzq.iPAccountDetection.Service;

import org.bukkit.entity.Player;
import org.zzq.iPAccountDetection.infrastructure.LogUtil;
import org.zzq.iPAccountDetection.model.aggregate.Group;
import org.zzq.iPAccountDetection.model.entity.Account;
import org.zzq.iPAccountDetection.model.repository.IGroupRepository;
import org.zzq.iPAccountDetection.model.repository.ILimitedAccountRepository;
import org.zzq.iPAccountDetection.model.repository.IMemoryGroupRepository;

import java.util.List;
import java.util.stream.Collectors;

public class DetectionService {
    private IMemoryGroupRepository memoryGroupRepository;
    private IGroupRepository groupRepository;
    private ILimitedAccountRepository limitedAccountRepository;
    private String adminPermission;
    private LogUtil logger;

    public DetectionService(IMemoryGroupRepository memoryGroupRepository, IGroupRepository groupRepository, ILimitedAccountRepository limitedAccountRepository, String adminPermission, LogUtil logger) {
        this.memoryGroupRepository = memoryGroupRepository;
        this.groupRepository = groupRepository;
        this.limitedAccountRepository = limitedAccountRepository;
        this.adminPermission = adminPermission;
        this.logger = logger;
    }

    public void handlePlayerLogin(Player player, String ipAddress) {
        String playerName = player.getName();

        String accountGroupId = memoryGroupRepository.getAccountGroupId(player.getUniqueId().toString());
        String ipGroupId = memoryGroupRepository.getIPGroupId(ipAddress);

        if (accountGroupId != null && ipGroupId != null && !accountGroupId.equals(ipGroupId)) {
            logger.info("名称: " + playerName + " ip: " + ipAddress + "都存在且不在同一组" +
                    "账号组id: " + accountGroupId + "账号: " + memoryGroupRepository.getGroupById(accountGroupId).getAccounts().stream().map(Account::getAccountName).collect(Collectors.joining(", ")) +
                    " ip组id: " + ipGroupId + " " + memoryGroupRepository.getGroupById(ipGroupId).getAccounts().stream().map(Account::getAccountName).collect(Collectors.joining(", ")));
            handleAllExistsLimit(player);
        } else if (accountGroupId != null && ipGroupId == null) {
            logger.info("名称: " + playerName + " ip: " + ipAddress + "只存在账号");
            handleOnlyAccount(ipAddress, accountGroupId);
        } else if (accountGroupId == null && ipGroupId != null) {
            logger.info("名称: " + playerName + " ip: " + ipAddress + "只存在ip");
            handelOnlyIp(player, ipGroupId, ipAddress);
        } else if (accountGroupId == null && ipGroupId == null) {
            logger.info("名称: " + playerName + " ip: " + ipAddress + "都不存在");
            handleInexistence(player, ipAddress);
        } else{
            logger.info("名称: " + playerName + " ip: " + ipAddress + "存在且在同一组");
            handleAllExists(accountGroupId, ipAddress);
        }
    }

    private void handleAllExistsLimit(Player player){
        if(player.hasPermission(adminPermission)) return;
        logger.info("加入 玩家: " + player.getName() + " 至限制组");
        limitedAccountRepository.addLimitedAccount(player.getUniqueId().toString(), player.getName());
    }

    private void handleOnlyAccount(String ipAddress, String accountGroupId){
        long time = System.currentTimeMillis();
        memoryGroupRepository.addGroupIP(accountGroupId, ipAddress, time);
        groupRepository.insertOrUpdateIP(ipAddress, accountGroupId, time);
    }

    private void handelOnlyIp(Player player, String ipGroupId, String ipAddress){
        Group group = memoryGroupRepository.getGroupById(ipGroupId);
        if(group.canAddAccount() || player.hasPermission(adminPermission)){
            long time = System.currentTimeMillis();
            memoryGroupRepository.updateIPLastLogin(ipGroupId, ipAddress, time);
            groupRepository.insertOrUpdateIP(ipAddress, ipGroupId, time);
            boolean isMain = !memoryGroupRepository.getGroupById(ipGroupId).hasMainAccount();
            memoryGroupRepository.addGroupAccount(ipGroupId, player.getUniqueId().toString(), player.getName(), isMain);
            groupRepository.addAccount(player.getUniqueId().toString(), player.getName(), ipGroupId, isMain);
        }else{
            limitedAccountRepository.addLimitedAccount(player.getUniqueId().toString(), player.getName());
        }
    }

    private void handleInexistence(Player player, String ipAddress){
        long time = System.currentTimeMillis();
        Group group = Group.createGroup(player.getUniqueId().toString(), player.getName(), ipAddress, time);
        memoryGroupRepository.addGroup(group);
        groupRepository.addGroup(group.getGroupId(),
                group.getMaxAccount(),
                group.isBan(),
                group.getAccounts().getFirst().getAccountId(),
                group.getAccounts().getFirst().getAccountName(),
                group.getAccounts().getFirst().isMain(),
                group.getIps().getFirst().getIp(),
                group.getIps().getFirst().getLastLogin());
    }

    private void handleAllExists(String groupId, String ipAddress){
        long time = System.currentTimeMillis();
        memoryGroupRepository.updateIPLastLogin(groupId, ipAddress, time);
        groupRepository.insertOrUpdateIP(ipAddress, groupId, time);
    }
}