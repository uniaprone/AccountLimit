package org.zzq.iPAccountDetection.Manager;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.zzq.iPAccountDetection.model.Group;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GroupManager {
    private Plugin plugin;
    private DatabaseManager databaseManager;
    private final Map<String, Group> groups = new ConcurrentHashMap<>();
    private final Map<String, String> accountToGroupMap = new ConcurrentHashMap<>();
    private final Map<String, String> ipToGroupMap = new ConcurrentHashMap<>();

    public GroupManager(Plugin plugin, DatabaseManager databaseManager){
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        setupCleanupTask();
        loadGroupsFromDatabase();
    }

    private void setupCleanupTask() {
        // 每6小时执行一次清理任务
        new BukkitRunnable() {
            @Override
            public void run() {
                databaseManager.cleanupOldIPs();
                // 重新加载IP映射，因为可能有IP被清理
                reloadIPMappings();
            }
        }.runTaskTimer(plugin, 0L, 20L * 60 * 60 * 6); // 6小时
    }

    private void loadGroupsFromDatabase() {
        try (Connection conn = databaseManager.getConnection()) {
            // 加载组基本信息
            String groupSql = "SELECT * FROM groups";
            try (PreparedStatement pstmt = conn.prepareStatement(groupSql)) {
                ResultSet rs = pstmt.executeQuery();
                boolean hasData = false;

                while (rs.next()) {
                    hasData = true;
                    String groupId = rs.getString("group_id");
                    int maxAccount = rs.getInt("max_account");

                    Group group = new Group(groupId);
                    group.setMaxAccount(maxAccount);

                    // 加载账号
                    loadAccountsForGroup(conn, group);
                    // 加载IP（从ip_login_times表）
                    loadIPsForGroup(conn, group);

                    groups.put(groupId, group);
                }

                if (hasData) {
                    plugin.getLogger().info("从数据库加载了 " + groups.size() + " 个组的数据");
                } else {
                    plugin.getLogger().info("数据库中暂无组数据");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("从数据库加载数据失败: " + e.getMessage());
        }
    }

    private void loadAccountsForGroup(Connection conn, Group group) throws SQLException {
        String sql = "SELECT account_name FROM accounts WHERE group_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, group.getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String account = rs.getString("account_name");
                group.addAccount(account);
                accountToGroupMap.put(account, group.getId());
            }
        }
    }

    private void loadIPsForGroup(Connection conn, Group group) throws SQLException {
        String sql = "SELECT ip FROM ip_login_times WHERE group_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, group.getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String ip = rs.getString("ip");
                group.addIP(ip);
                ipToGroupMap.put(ip, group.getId());
            }
        }
    }

    private void reloadIPMappings() {
        ipToGroupMap.clear();
        for (Group group : groups.values()) {
            group.getIPs().clear();
            try (Connection conn = databaseManager.getConnection()) {
                loadIPsForGroup(conn, group);
            } catch (SQLException e) {
                plugin.getLogger().severe("重新加载IP映射失败: " + e.getMessage());
            }
        }
    }

    public void updateIPLoginTime(String ip, String groupId) {
        databaseManager.updateIPLoginTime(ip, groupId);
    }

    public boolean removeAccount(String account){
        if(account == null) return false;
        Group group = getGroupByPlayer(account);
        if(group == null) return false;
        group.removeAccount(account);
        if(accountToGroupMap.get(account) == null) return false;
        accountToGroupMap.remove(account);
        databaseManager.removeAccount(account);
        return true;
    }

    public boolean addAccount(String addedAccount, String addAccount){
        if(addedAccount == null && addAccount == null) return false;
        Group group = getGroupByPlayer(addedAccount);
        if(group == null) return false;
        group.addAccount(addAccount);
        accountToGroupMap.put(addAccount, group.getId());
        databaseManager.addAccount(addedAccount, addAccount);
        return true;
    }
    public boolean setMaxAccount(String account, int count){
        if(account == null) return false;
        Group group = getGroupByPlayer(account);
        if(group == null) return false;
        group.setMaxAccount(count);
        databaseManager.setMaxAccount(group.getId(), count);
        return true;
    }
    public void saveData() {
        // 保存到数据库
        saveAllGroupsToDatabase();
        plugin.getLogger().info("已保存 " + groups.size() + " 个组的数据到数据库");
    }

    private void saveAllGroupsToDatabase() {
        for (Group group : groups.values()) {
            databaseManager.saveGroupToDatabase(
                    group.getId(),
                    group.getMaxAccount(),
                    new ArrayList<>(group.getAccounts()),
                    new ArrayList<>(group.getIPs())
            );
        }
    }

    // 添加组
    public void addGroup(String groupId, int maxAccount, List<String> accounts, List<String> ips) {
        Group group = new Group(groupId);
        group.setMaxAccount(maxAccount);

        for (String account : accounts) {
            group.addAccount(account);
            accountToGroupMap.put(account, groupId);
        }

        for (String ip : ips) {
            group.addIP(ip);
            ipToGroupMap.put(ip, groupId);
            // 更新IP登录时间
            databaseManager.updateIPLoginTime(ip, groupId);
        }

        groups.put(groupId, group);
        saveData();
    }

    private void deleteGroupFromDatabase(String groupId) {
        String deleteGroupSql = "DELETE FROM groups WHERE group_id = ?";
        String deleteAccountsSql = "DELETE FROM accounts WHERE group_id = ?";
        String deleteIPsSql = "DELETE FROM ip_login_times WHERE group_id = ?";

        try (Connection conn = databaseManager.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(deleteAccountsSql)) {
                pstmt.setString(1, groupId);
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(deleteIPsSql)) {
                pstmt.setString(1, groupId);
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(deleteGroupSql)) {
                pstmt.setString(1, groupId);
                pstmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            plugin.getLogger().severe("从数据库删除组失败: " + e.getMessage());
        }
    }

    // 向组添加账号
    public void addAccountToGroup(String groupId, String account) {
        Group group = groups.get(groupId);
        if (group != null) {
            group.addAccount(account);
            accountToGroupMap.put(account, groupId);
            saveData();
        }
    }
    // 基础查询方法
    public Group getGroupByPlayer(String playerName) {
        String groupId = accountToGroupMap.get(playerName);
        return groupId != null ? groups.get(groupId) : null;
    }

    public int getTotalGroups() {
        return groups.size();
    }

    public Map<String, Group> getGroups() {
        return groups;
    }

    public Map<String, String> getAccountToGroupMap() {
        return accountToGroupMap;
    }

    public Map<String, String> getIpToGroupMap() {
        return ipToGroupMap;
    }

    public void setLimit(String player, int maxCount) {
        if (player == null) return;
        String groupID = accountToGroupMap.get(player);
        if (groupID != null && groups.containsKey(groupID)) {
            groups.get(groupID).setMaxAccount(maxCount);
            saveData();
        }
    }

    public void removePlayer(String player) {
        if (player == null) return;
        String groupID = accountToGroupMap.get(player);
        if (groupID != null && groups.containsKey(groupID)) {
            groups.get(groupID).removeAccount(player);
            accountToGroupMap.remove(player);
            saveData();
        }
    }

    public void shutdown() {
        // 关闭前保存数据
        saveData();
        databaseManager.close();
    }

    public void reload(){
        loadGroupsFromDatabase();
    }
}