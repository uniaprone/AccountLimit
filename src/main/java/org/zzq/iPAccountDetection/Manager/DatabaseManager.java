package org.zzq.iPAccountDetection.Manager;

import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.List;
import java.util.logging.Logger;

public class DatabaseManager {
    private Connection connection;
    private final String dbPath;
    private final Logger logger;

    public DatabaseManager(Plugin plugin) {
        this.dbPath = plugin.getDataFolder().getAbsolutePath() + "/data.db";
        this.logger = plugin.getLogger();
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
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
                    account_name TEXT PRIMARY KEY,
                    group_id TEXT NOT NULL,
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
            logger.severe("数据库初始化失败: " + e.getMessage());
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
            logger.severe("更新IP登录时间失败: " + e.getMessage());
        }
    }

    // 获取IP的最后登录时间
    public long getIPLastLoginTime(String ip) {
        String sql = "SELECT last_login FROM ips WHERE ip = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, ip);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("last_login");
            }
        } catch (SQLException e) {
            logger.severe("获取IP登录时间失败: " + e.getMessage());
        }
        return 0;
    }

    // 清理30天前的IP记录
    public void cleanupOldIPs() {
        long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
        String sql = "DELETE FROM ips WHERE last_login < ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, thirtyDaysAgo);
            int deleted = pstmt.executeUpdate();
            if (deleted > 0) {
                logger.info("清理了 " + deleted + " 个过期IP记录");
            }
        } catch (SQLException e) {
            logger.severe("清理过期IP记录失败: " + e.getMessage());
        }
    }

    // 保存组数据到数据库
    public void saveGroupToDatabase(String groupId, int maxAccount, List<String> accounts, List<String> ips) {
        String groupSql = "INSERT OR REPLACE INTO groups (group_id, max_account) VALUES (?, ?)";
        String accountSql = "INSERT OR REPLACE INTO accounts (account_name, group_id) VALUES (?, ?)";
        String ipSql = "INSERT OR REPLACE INTO ips (ip, group_id, last_login) VALUES (?, ?, ?)";

        try (Connection conn = getConnection()) {
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
                }
            }

            conn.commit();
        } catch (SQLException e) {
            logger.severe("保存组数据到数据库失败: " + e.getMessage());
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
            logger.severe("添加账号失败: " + e.getMessage());
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
            logger.severe("删除账号失败: " + e.getMessage());
            return false;
        }
    }

    public boolean setMaxAccount(String groupId, int count) {
        String sql = "INSERT OR REPLACE INTO groups (group_id, count) VALUES (?, ?)";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, groupId);
            pstmt.setInt(2, count);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.severe("添加账号失败: " + e.getMessage());
            return false;
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.severe("关闭数据库连接失败: " + e.getMessage());
        }
    }
}