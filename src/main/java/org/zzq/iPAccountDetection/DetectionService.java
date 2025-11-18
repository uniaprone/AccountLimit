package org.zzq.iPAccountDetection;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.zzq.iPAccountDetection.AlertService.AlertService;

import java.util.*;

public class DetectionService {
    private final JavaPlugin plugin;
    private ConfigManager configManager;
    private GroupManager groupManager;
    private int maxAccounts;
    private final Map<String, Group> groups;
    private final Map<String, String> accountToGroupMap;
    private final Map<String, String> ipToGroupMap;
    private AlertService alertService;

    public DetectionService(JavaPlugin plugin, ConfigManager configManager, GroupManager groupManager, AlertService alertService) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.groupManager = groupManager;
        this.maxAccounts = configManager.getMaxAccount();
        this.groups = groupManager.getGroups();
        this.accountToGroupMap = groupManager.getAccountToGroupMap();
        this.ipToGroupMap = groupManager.getIpToGroupMap();
        this.alertService = alertService;
    }

    public void handlePlayerLogin(Player player, String ipAddress) {
        String playerName = player.getName();
        // 检查账号和IP是否已存在
        String accountGroupId = accountToGroupMap.get(playerName);
        String ipGroupId = ipToGroupMap.get(ipAddress);
        //账号，ip都存在，且不在同一组
        if (accountGroupId != null && ipGroupId != null && !accountGroupId.equals(ipGroupId)) {
            handleAllExists(player, ipGroupId);
        } else if (accountGroupId != null) {
            handleOnlyAccount(ipAddress, accountGroupId);
        } else if (ipGroupId != null) {
            handelOnlyIp(player, ipGroupId);
        } else {
            handleInexistence(playerName, ipAddress);
        }
//
//        // 检查阈值并发出警告
//        checkAndWarn(playerName);
    }

    private void addToExistingGroup(String groupId, String playerName, String ipAddress) {
        Group group = groups.get(groupId);
        if (group != null) {
            group.addAccount(playerName);
            group.addIP(ipAddress);

            // 更新映射
            accountToGroupMap.put(playerName, groupId);
            ipToGroupMap.put(ipAddress, groupId);
        }
    }

    private void mergeGroups(String groupId1, String groupId2) {
        Group group1 = groups.get(groupId1);
        Group group2 = groups.get(groupId2);

        if (group1 == null || group2 == null) return;

        // 将group2的所有内容合并到group1
        for (String account : group2.getAccounts()) {
            group1.addAccount(account);
            accountToGroupMap.put(account, groupId1);
        }

        for (String ip : group2.getIPs()) {
            group1.addIP(ip);
            ipToGroupMap.put(ip, groupId1);
        }

        // 移除group2
        groups.remove(groupId2);
    }

//    private void checkAndWarn(String playerName) {
//        String groupId = accountToGroupMap.get(playerName);
//        if (groupId == null) return;
//
//        Group group = groups.get(groupId);
//        if (group != null && group.getAccounts().size() >= maxAccounts) {
//            // 向所有在线管理员发送警告
//            String message = String.format("§c警告: 账号 %s 关联了 %d 个账号 (组: %s)",
//                    playerName, group.getAccounts().size(), groupId);
//
//            plugin.getServer().getOnlinePlayers().stream()
//                    .filter(p -> p.hasPermission("ipaccountdetection.notify"))
//                    .forEach(p -> p.sendMessage(message));
//
//            plugin.getLogger().warning(message);
//            String command = "ban " + playerName + " 使用过多关联账号";
//            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
//        }
//    }
    private void handleAllExists(Player player,String ipGroupId){
        if(ipGroupId == null) return;
        Group ipGroup = groups.get(ipGroupId);
        int maxAccount = ipGroup.getMaxAccount();
        Set<String> accounts = ipGroup.getAccounts();
        openAlertGUI(player, accounts);
    }

    private void handleOnlyAccount(String ipAddress, String accountGroupId){
        Group accountGroup = groups.get(accountGroupId);
        accountGroup.getIPs().add(ipAddress);
    }

    private void handelOnlyIp(Player player, String ipGroupId){
        Group ipGroup = groups.get(ipGroupId);
        int maxAccount = ipGroup.getMaxAccount();
        Set<String> accounts = ipGroup.getAccounts();
        if(accounts.size() < maxAccount){
            accounts.add(player.getName());
        }else{
            openAlertGUI(player, accounts);
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
        plugin.getLogger().info("组: " + groupId + "账号: " + playerName + "ip: " + ipAddress);
    }

    public void openAlertGUI(Player player, Set<String> accounts){
        alertService.openAlertGUI(player, accounts);
    }
}