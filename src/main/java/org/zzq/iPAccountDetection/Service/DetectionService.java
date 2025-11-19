package org.zzq.iPAccountDetection.Service;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.zzq.iPAccountDetection.command.CommandCompleter;
import org.zzq.iPAccountDetection.model.Group;
import org.zzq.iPAccountDetection.Manager.GroupManager;

import java.util.*;

public class DetectionService {
    private final JavaPlugin plugin;
    private final GroupManager groupManager;
    private final Map<String, Group> groups;
    private final Map<String, String> accountToGroupMap;
    private final Map<String, String> ipToGroupMap;
    private LimitDataService limitDataService;
    private String adminPermission;

    public DetectionService(JavaPlugin plugin, GroupManager groupManager, LimitDataService limitDataService) {
        this.plugin = plugin;
        this.groupManager = groupManager;
        this.groups = groupManager.getGroups();
        this.accountToGroupMap = groupManager.getAccountToGroupMap();
        this.ipToGroupMap = groupManager.getIpToGroupMap();
        this.limitDataService = limitDataService;
        this.adminPermission = CommandCompleter.adminPermission;
    }

    public void handlePlayerLogin(Player player, String ipAddress) {
        String playerName = player.getName();

        // 更新IP登录时间（无论IP是否已存在）
        updateIPLoginTime(ipAddress, playerName);

        String accountGroupId = accountToGroupMap.get(playerName);
        String ipGroupId = ipToGroupMap.get(ipAddress);

        plugin.getLogger().info("存在: " + groups);

        if (accountGroupId != null && ipGroupId != null && !accountGroupId.equals(ipGroupId)) {
            plugin.getLogger().info("1");
            handleAllExists(player, ipGroupId);
        } else if (accountGroupId != null) {
            plugin.getLogger().info("2");
            handleOnlyAccount(ipAddress, accountGroupId);
        } else if (ipGroupId != null) {
            plugin.getLogger().info("3");
            handelOnlyIp(player, ipGroupId);
        } else {
            plugin.getLogger().info("4");
            handleInexistence(playerName, ipAddress);
        }
    }

    private void updateIPLoginTime(String ip, String playerName) {
        String groupId = accountToGroupMap.get(playerName);
        if (groupId != null) {
            groupManager.updateIPLoginTime(ip, groupId);
        } else {
            // 如果玩家还没有组，先创建组再更新
            String ipGroupId = ipToGroupMap.get(ip);
            if (ipGroupId != null) {
                groupManager.updateIPLoginTime(ip, ipGroupId);
            }
        }
    }

    private void handleAllExists(Player player, String ipGroupId){
        if(ipGroupId == null) return;
        if(player.hasPermission(adminPermission)) return;
        limitDataService.addLimitAccount(player.getName());
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
            limitDataService.addLimitAccount(player.getName());
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

        plugin.getLogger().info("组: " + groupId + "账号: " + playerName + "ip: " + ipAddress);
    }
}