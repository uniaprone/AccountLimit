package org.zzq.iPAccountDetection.Service;

import org.bukkit.entity.Player;
import org.zzq.iPAccountDetection.command.CommandCompleter;
import org.zzq.iPAccountDetection.infrastructure.AccountLogger;
import org.zzq.iPAccountDetection.model.Group;
import org.zzq.iPAccountDetection.Manager.GroupManager;
import org.zzq.iPAccountDetection.model.LimitAccount;

import java.util.*;

public class DetectionService {
    private final AccountLogger accountLogger;
    private final GroupManager groupManager;
    private final Map<String, Group> groups;
    private final Map<String, String> accountToGroupMap;
    private final Map<String, String> ipToGroupMap;
    private LimitAccount limitAccount;
    private String adminPermission;

    public DetectionService(AccountLogger accountLogger, GroupManager groupManager, LimitAccount limitAccount) {
        this.accountLogger = accountLogger;
        this.groupManager = groupManager;
        this.groups = groupManager.getGroups();
        this.accountToGroupMap = groupManager.getAccountToGroupMap();
        this.ipToGroupMap = groupManager.getIpToGroupMap();
        this.limitAccount = limitAccount;
        this.adminPermission = CommandCompleter.adminPermission;
    }

    public void handlePlayerLogin(Player player, String ipAddress) {
        String playerName = player.getName();

        String accountGroupId = accountToGroupMap.get(playerName);
        String ipGroupId = ipToGroupMap.get(ipAddress);

        if (accountGroupId != null && ipGroupId != null && !accountGroupId.equals(ipGroupId)) {
            accountLogger.debug("名称: " + playerName + " ip: " + ipAddress + "都存在且不在同一组");
            handleAllExists(player, ipGroupId);
        } else if (accountGroupId != null && ipGroupId == null) {
            accountLogger.debug("名称: " + playerName + " ip: " + ipAddress + "只存在账号");
            handleOnlyAccount(ipAddress, accountGroupId);
        } else if (accountGroupId == null && ipGroupId != null) {
            accountLogger.debug("名称: " + playerName + " ip: " + ipAddress + "只存在ip");
            handelOnlyIp(player, ipGroupId);
        } else if (accountGroupId == null && ipGroupId == null) {
            accountLogger.debug("名称: " + playerName + " ip: " + ipAddress + "都不存在");
            handleInexistence(playerName, ipAddress);
        } else{
            accountLogger.debug("名称: " + playerName + " ip: " + ipAddress + "存在且在同一组");
            groupManager.updateIPLoginTime(ipAddress, accountGroupId);
        }
    }

    private void handleAllExists(Player player, String ipGroupId){
        if(ipGroupId == null) return;
        if(player.hasPermission(adminPermission)) return;
        accountLogger.debug("加入 玩家: " + player.getName() + " 至限制组");
        limitAccount.addLimitAccount(player.getName());
    }

    private void handleOnlyAccount(String ipAddress, String accountGroupId){
        Group accountGroup = groups.get(accountGroupId);
        accountGroup.getIPs().add(ipAddress);
        ipToGroupMap.put(ipAddress, accountGroupId);
        groupManager.updateIPLoginTime(ipAddress, accountGroupId);
    }

    private void handelOnlyIp(Player player, String ipGroupId){
        Group ipGroup = groups.get(ipGroupId);
        int maxAccount = ipGroup.getMaxAccount();
        Set<String> accounts = ipGroup.getAccounts();
        List<String> accountList = new ArrayList<>(accounts);

        if(accounts.size() < maxAccount || player.hasPermission(adminPermission)){
            accounts.add(player.getName());
            accountToGroupMap.put(player.getName(), ipGroupId);
            groupManager.addAccount(accountList.getFirst(), player.getName());
            groupManager.updateIPLoginTime(player.getAddress().getAddress().getHostAddress(), ipGroupId);
        }else{
            limitAccount.addLimitAccount(player.getName());
        }
    }

    private void handleInexistence(String playerName, String ipAddress){
        createNewGroup(playerName, ipAddress);
    }

    private void createNewGroup(String playerName, String ipAddress) {
        String groupId = UUID.randomUUID().toString();
        Group group = new Group(groupId);
        group.addAccount(playerName);
        group.addIP(ipAddress);

        groups.put(groupId, group);
        accountToGroupMap.put(playerName, groupId);
        ipToGroupMap.put(ipAddress, groupId);

        // 更新数据库中的登录时间
        groupManager.updateIPLoginTime(ipAddress, groupId);

        accountLogger.debug("创建了组: " + groupId + " 账号: " + playerName + " ip: " + ipAddress);
    }
}