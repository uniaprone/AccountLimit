package org.zzq.iPAccountDetection;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GroupManager {
    private Plugin plugin;
    private File file;
    private FileConfiguration fileConfiguration;
    private final Map<String, Group> groups = new ConcurrentHashMap<>();
    private final Map<String, String> accountToGroupMap = new ConcurrentHashMap<>();
    private final Map<String, String> ipToGroupMap = new ConcurrentHashMap<>();

    public GroupManager(Plugin plugin){
        this.plugin = plugin;
        loadGroup();
    }

    private void loadGroup(){
        file = new File(plugin.getDataFolder(), "groups.yml");
        if(!file.exists()){
            plugin.saveResource(file.getName(), false);
        }
        fileConfiguration = YamlConfiguration.loadConfiguration(file);
        parseGroup();
    }

    private void parseGroup(){
        if(!fileConfiguration.contains("groups")){
            plugin.getLogger().info("未找到任何组");
            return;
        }
        ConfigurationSection groupsSection = fileConfiguration.getConfigurationSection("groups");
        if (groupsSection == null) return;
        for(String groupId : groupsSection.getKeys(false)){
            String path = "groups." + groupId;
            Group group = new Group(groupId);
            group.setMaxAccount(fileConfiguration.getInt(path + ".maxAccount"));
            List<String> accounts = fileConfiguration.getStringList(path + ".accounts");
            for (String account : accounts){
                group.addAccount(account);
                accountToGroupMap.put(account, groupId);
            }
            List<String> ips = fileConfiguration.getStringList(path + ".ips");
            for(String ip: ips){
                group.addIP(ip);
                ipToGroupMap.put(ip, groupId);
            }
            groups.put(groupId, group);
        }
        plugin.getLogger().info("已加载 " + groups.size() + " 个组的数据");
    }

    public void saveData() {
        // 清空现有数据
        fileConfiguration.set("groups", null);
        plugin.getLogger().info("保存");
        // 保存组数据
        for (Map.Entry<String, Group> entry : groups.entrySet()) {

            String groupId = entry.getKey();
            Group group = entry.getValue();
            plugin.getLogger().info("数据： " + groupId);
            ConfigurationSection groupSection = fileConfiguration.createSection("groups." + groupId);
            groupSection.set("maxAccount", group.getMaxAccount());
            groupSection.set("accounts", new ArrayList<>(group.getAccounts()));
            groupSection.set("ips", new ArrayList<>(group.getIPs()));
        }

        // 保存配置
        try {
            fileConfiguration.save(file); // 这才是保存到groups.yml
            plugin.getLogger().info("已保存 " + groups.size() + " 个组的数据");
        } catch (IOException e) {
            plugin.getLogger().severe("保存groups.yml失败: " + e.getMessage());
        }

    }

    public Group getGroupByPlayer(String playerName) {
        String groupId = accountToGroupMap.get(playerName);
        return groupId != null ? groups.get(groupId) : null;
    }

    public Group getGroupByIP(String ip) {
        String groupId = ipToGroupMap.get(ip);
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
}
