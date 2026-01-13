package org.zzq.iPAccountDetection.infrastructure;

import org.bukkit.plugin.Plugin;
import org.slf4j.LoggerFactory;
import org.zzq.iPAccountDetection.model.entity.Account;
import org.zzq.iPAccountDetection.model.aggregate.Group;
import org.zzq.iPAccountDetection.model.entity.IP;
import org.zzq.iPAccountDetection.model.repository.IGroupRepository;

import java.sql.*;
import java.util.*;

public class GroupDatabase implements IGroupRepository {
    private static final long THIRTY_DAYS_IN_MILLIS = 30L * 24 * 60 * 60 * 1000;
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(GroupDatabase.class);

    private Connection connection;
    private final String dbPath;
    private final LogUtil logger;

    public GroupDatabase(Plugin plugin, LogUtil logger) {
        this.dbPath = plugin.getDataFolder().getAbsolutePath() + "/data.db";
        this.logger = logger;
        initializeDatabase();
    }

    private void initializeDatabase() {
        try{
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);

            // 创建IP登录时间表
            String createIPTable = """
                CREATE TABLE IF NOT EXISTS ips (
                    ip TEXT PRIMARY KEY,
                    group_id TEXT NOT NULL,
                    last_login INTEGER NOT NULL,
                    FOREIGN KEY (group_id) REFERENCES groups(group_id)
                )
            """;

            // 创建组表（用于存储组基本信息）
            String createGroupTable = """
                CREATE TABLE IF NOT EXISTS groups (
                    group_id TEXT PRIMARY KEY,
                    max_account INTEGER DEFAULT 2
                )
            """;

            // 创建账号表
            String createAccountTable = """
                CREATE TABLE IF NOT EXISTS accounts (
                    account_id TEXT PRIMARY KEY,
                    account_name TEXT NOT NULL,
                    group_id TEXT NOT NULL,
                    is_main BOOLEAN NOT NULL DEFAULT FALSE,
                    FOREIGN KEY (group_id) REFERENCES groups(group_id)
                )
            """;

            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createGroupTable);
                stmt.execute(createAccountTable);
                stmt.execute(createIPTable);
            }

            logger.info("数据库初始化完成");

        } catch (Exception e) {
            logger.error("数据库初始化失败: " + e.getMessage());
        }
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        }
        return connection;
    }

    // 更新IP登录时间
    public void updateIPLoginTime(String ip, String groupId) {
        if(ip == null || groupId == null) return;
        String sql = "INSERT OR REPLACE INTO ips (ip, group_id, last_login) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, ip);
            pstmt.setString(2, groupId);
            pstmt.setLong(3, System.currentTimeMillis());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("更新IP登录时间失败: " + e.getMessage());
        }
    }

    // 清理30天前的IP记录
    public boolean cleanupExpiredIPs(List<IP> expiredIPs) {
        if (expiredIPs.isEmpty()) {
            return true;
        }

        String sql = "DELETE FROM ips WHERE ip = ? AND group_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (IP ip : expiredIPs) {
                pstmt.setString(1, ip.getIp());
                pstmt.setString(2, ip.getGroupId());
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            conn.commit();

            int successCount = (int) Arrays.stream(results).filter(r -> r > 0).count();
            logger.info("从数据库成功删除 " + successCount + " 个IP记录");

            return successCount == expiredIPs.size();

        } catch (SQLException e) {
            logger.error("从数据库删除IP失败: " + e.getMessage());
            return false;
        }
    }

    // 保存组数据到数据库
    public void saveGroupToDatabase(String groupId, int maxAccount, List<String> accounts, List<String> ips) {
        String groupSql = "INSERT OR REPLACE INTO groups (group_id, max_account) VALUES (?, ?)";
        String accountSql = "INSERT OR REPLACE INTO accounts (account_name, group_id) VALUES (?, ?)";
        String ipSql = "INSERT OR REPLACE INTO ips (ip, group_id, last_login) VALUES (?, ?, ?)";
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // 保存组基本信息
            try (PreparedStatement pstmt = conn.prepareStatement(groupSql)) {
                pstmt.setString(1, groupId);
                pstmt.setInt(2, maxAccount);
                pstmt.executeUpdate();
            }

            // 保存账号信息
            try (PreparedStatement pstmt = conn.prepareStatement(accountSql)) {
                for (String account : accounts) {
                    pstmt.setString(1, account);
                    pstmt.setString(2, groupId);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(ipSql)){
                for(String ip : ips){
                    pstmt.setString(1, ip);
                    pstmt.setString(2, groupId);
                    pstmt.setLong(3, System.currentTimeMillis());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            conn.commit();
        } catch (SQLException e) {
            logger.error("保存组数据到数据库失败: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback(); // 回滚事务[2](@ref)
                } catch (SQLException rollbackEx) {
                    logger.error("回滚事务失败: " + rollbackEx.getMessage());
                }
            }
        }
    }

    public boolean addAccount(String accountName, String groupId) {
        String sql = "INSERT OR REPLACE INTO accounts (account_name, group_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, accountName);
            pstmt.setString(2, groupId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("添加账号失败: " + e.getMessage());
            return false;
        }
    }

    public boolean removeAccount(String accountName) {
        String sql = "DELETE FROM accounts WHERE account_name = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, accountName);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("删除账号失败: " + e.getMessage());
            return false;
        }
    }

    public boolean removeIP(String ip) {
        String sql = "DELETE FROM ips WHERE ip = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, ip);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("数据库删除ip失败: " + e.getMessage());
            return false;
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.error("关闭数据库连接失败: " + e.getMessage());
        }
    }

    public Map<String, Group> loadGroup(){
        Map<String, Group> groupMap = new HashMap<>();
        String sql = "SELECT * FROM groups";
        try(PreparedStatement pstmt = getConnection().prepareStatement(sql)){
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                String groupId = rs.getString("group_id");
                int maxAccount = rs.getInt("max_account");
                Group group = new Group(groupId, maxAccount);
                groupMap.put(groupId, group);
            }
            logger.info("成功加载了 " + groupMap.size() + "个组数据");
            return groupMap;
        }catch (SQLException e){
            logger.error("加载组数据失败: " + e.getMessage());
            return groupMap;
        }
    }

    public boolean insertOrReplaceGroup(String groupId, int count) {
        String sql = "INSERT OR REPLACE INTO groups (group_id, max_account) VALUES (?, ?)";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, groupId);
            pstmt.setInt(2, count);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("设置最大账号数量失败: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteGroup(String groupId) {
        String groupSql = "DELETE FROM groups WHERE group_id = ?";
        String accountSql = "DELETE FROM accounts WHERE group_id = ?";
        String ipSql = "DELETE FROM ips WHERE group_id = ?";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // 1. 删除ips表中的相关数据
            try (PreparedStatement pstmt = conn.prepareStatement(ipSql)) {
                pstmt.setString(1, groupId);
                pstmt.executeUpdate();
            }

            // 2. 删除accounts表中的相关数据
            try (PreparedStatement pstmt = conn.prepareStatement(accountSql)) {
                pstmt.setString(1, groupId);
                pstmt.executeUpdate();
            }

            // 3. 最后删除groups表中的组记录本身
            try (PreparedStatement pstmt = conn.prepareStatement(groupSql)) {
                pstmt.setString(1, groupId);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected == 0) {
                    logger.warn("未找到group_id为 " + groupId + " 的组记录。");
                }
            }

            conn.commit();
            logger.info("组 " + groupId + " 及其相关数据删除成功。");
            return true;
        } catch (SQLException e) {
            logger.error("删除组数据失败: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.info("事务已回滚。");
                } catch (SQLException rollbackEx) {
                    logger.error("回滚事务失败: " + rollbackEx.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logger.error("关闭数据库连接失败: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public boolean addAccount(String accountId, String accountName, String groupId, boolean isMain) {
        String sql = "INSERT OR REPLACE INTO accounts (account_id, account_name, group_id, is_main) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            pstmt.setString(2, accountName);
            pstmt.setString(3, groupId);
            pstmt.setBoolean(4, isMain);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("添加账号失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteAccount(String accountId) {
        String sql = "DELETE FROM accounts WHERE account_id = ?";
        try(PreparedStatement pstmt = getConnection().prepareStatement(sql)){
            pstmt.setString(1, accountId);
            int affectRow = pstmt.executeUpdate();
            if(affectRow > 0){
                logger.info("删除账号成功: " + accountId);
                return true;
            }else{
                logger.error("删除账号失败： " + affectRow);
                return false;
            }
        }catch (SQLException e){
            logger.error("删除账号失败： " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean insertOrUpdateIP(String ip, String groupId, long time) {
        String sql = "INSERT OR REPLACE INTO ips (ip, group_id, last_login) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, ip);
            pstmt.setString(2, groupId);
            pstmt.setLong(3, time);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.error("插入或更新IP失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteIP(String ip) {
        String sql = "DELETE FROM ips WHERE ip = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, ip);
            int affectedRows = pstmt.executeUpdate();
            if(affectedRows < 0){
                logger.error("数据库删除ip失败: " + ip);
                return false;
            }
            return true;
        } catch (SQLException e) {
            logger.error("数据库删除ip失败: " + e.getMessage());
            return false;
        }
    }

    public boolean addGroup(String groupId, int maxAccount, String accountId, String accountName, boolean isMain, String ip, long lastLogin) {
        String groupSql = "INSERT OR REPLACE INTO groups (group_id, max_account) VALUES (?, ?)";
        String accountSql = "INSERT OR REPLACE INTO accounts (account_id, account_name, group_id, is_main) VALUES (?, ?, ?, ?)";
        String ipSql = "INSERT OR REPLACE INTO ips (ip, group_id, last_login) VALUES (?, ?, ?)";
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // 保存组基本信息
            try (PreparedStatement pstmt = conn.prepareStatement(groupSql)) {
                pstmt.setString(1, groupId);
                pstmt.setInt(2, maxAccount);
                pstmt.executeUpdate();
            }

            // 保存账号信息
            try (PreparedStatement pstmt = conn.prepareStatement(accountSql)) {
                pstmt.setString(1, accountId);
                pstmt.setString(2, accountName);
                pstmt.setString(3, groupId);
                pstmt.setBoolean(4, isMain);
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(ipSql)){
                pstmt.setString(1, ip);
                pstmt.setString(2, groupId);
                pstmt.setLong(3, lastLogin);
                pstmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            logger.error("保存组数据到数据库失败: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback(); // 回滚事务[2](@ref)
                } catch (SQLException rollbackEx) {
                    logger.error("回滚事务失败: " + rollbackEx.getMessage());
                }
            }
            return false;
        }
    }

    public GroupLoadResult loadGroupsWithAssociations(){
        Map<String, Group> groupMap = new HashMap<>();
        Map<String, String> accountMap = new HashMap<>();
        Map<String, String> ipMap = new HashMap<>();
        String sql = """
                SELECT g.group_id, g.max_account,
                a.account_id, a.account_name, a.is_main,
                i.ip,i.last_login
                FROM groups g
                LEFT JOIN accounts a ON g.group_id = a.group_id
                LEFT JOIN ips i ON g.group_id = i.group_id
                ORDER BY g.group_id
                """;
        try(PreparedStatement pstmt = getConnection().prepareStatement(sql)){
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()){
                String groupId = rs.getString("group_id");
                int maxAccount = rs.getInt("max_account");
                Group currentGroup = groupMap.computeIfAbsent(groupId, id -> {
                    Group newGroup = new Group();
                    newGroup.setGroupId(id);
                    newGroup.setMaxAccount(maxAccount);
                    newGroup.setAccounts(new ArrayList<>());
                    newGroup.setIps(new ArrayList<>());
                    return newGroup;
                });

                String accountId = rs.getString("account_id");
                if(accountId != null && !rs.wasNull()){
                    boolean accountExists = currentGroup.getAccounts().stream()
                            .anyMatch(account -> Objects.equals(account.getAccountId(), accountId));
                    if(!accountExists){
                        Account account = new Account();
                        account.setAccountId(accountId);
                        account.setAccountName(rs.getString("account_name"));
                        account.setMain(rs.getBoolean("is_main"));
                        account.setGroupId(groupId);
                        currentGroup.addAccounts(account);
                        accountMap.put(accountId, groupId);
                    }
                }

                String ip = rs.getString("ip");
                if(ip != null && !rs.wasNull()){
                    boolean ipExists = currentGroup.getIps().stream()
                            .anyMatch(ip1 -> Objects.equals(ip1.getIp(), ip));
                    if(!ipExists){
                        IP ipObj = new IP();
                        ipObj.setIp(ip);
                        ipObj.setLastLogin(rs.getLong("last_login"));
                        ipObj.setGroupId(groupId);
                        currentGroup.addIPs(ipObj);
                        ipMap.put(ip, groupId);
                    }
                }
            }
            return new GroupLoadResult(groupMap, accountMap, ipMap);
        }catch (SQLException e){
            logger.error("加载组时发生错误: " + e.getMessage());
            return new GroupLoadResult(groupMap, accountMap, ipMap);
        }
    }
    @Override
    public boolean setMainAccount(String accountId, String groupId){
        if (accountId == null || groupId == null) {
            logger.error("设置主账号失败: 参数不能为null");
            return false;
        }

        String clearMainSql = "UPDATE accounts SET is_main = false WHERE group_id = ?";
        String setMainSql = "UPDATE accounts SET is_main = true WHERE account_id = ? AND group_id = ?";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // 开始事务

            // 1. 先将该组内所有账号的 is_main 设置为 false
            try (PreparedStatement clearStmt = conn.prepareStatement(clearMainSql)) {
                clearStmt.setString(1, groupId);
                int clearedCount = clearStmt.executeUpdate();
                logger.warn("清除了组 " + groupId + " 中 " + clearedCount + " 个账号的主账号状态");
            }

            // 2. 将指定账号设置为唯一主账号
            try (PreparedStatement setStmt = conn.prepareStatement(setMainSql)) {
                setStmt.setString(1, accountId);
                setStmt.setString(2, groupId);
                int affectedRows = setStmt.executeUpdate();

                if (affectedRows == 0) {
                    // 没有更新任何行，说明账号不存在或不属于该组
                    conn.rollback();
                    logger.error("设置主账号失败: 账号 " + accountId + " 在组 " + groupId + " 中不存在");
                    return false;
                }

                logger.info("成功将账号 " + accountId + " 设置为主账号，组ID: " + groupId);
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            logger.error("设置主账号失败: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.info("事务已回滚");
                } catch (SQLException rollbackEx) {
                    logger.error("回滚事务失败: " + rollbackEx.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // 恢复自动提交
                    conn.close();
                } catch (SQLException e) {
                    logger.error("关闭数据库连接失败: " + e.getMessage());
                }
            }
        }
    }
}