package org.zzq.iPAccountDetection.infrastructure;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    private Plugin plugin;
    private File file;
    private FileConfiguration fileConfiguration;
    private final List<ConfigChangeListener> listeners = new ArrayList<>();

    private String env;
    private Integer maxAccount;
    private Long ipExpireMin;
    private Long ipClearMin;
    private boolean isLimitEnable;

    public ConfigManager(Plugin plugin){
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig(){
        file = new File(plugin.getDataFolder(), "config.yml");
        if(!file.exists()){
            plugin.saveResource(file.getName(), false);
            file = new File(plugin.getDataFolder(), "config.yml");
        }
        fileConfiguration = YamlConfiguration.loadConfiguration(file);
        parseConfig();
    }

    public void reload(){
        loadConfig();
    }

    public void parseConfig(){
        env = fileConfiguration.getString("env", "PRODUCTION");
        maxAccount = fileConfiguration.getInt("max_account_num", 2);
        ipExpireMin = fileConfiguration.getLong("ip_expire_min", 1440);
        ipClearMin = fileConfiguration.getLong("ip_clear_min", 30);
        isLimitEnable = fileConfiguration.getBoolean("enable_limit", false);
        notifyListeners();
    }

    public void registerListener(ConfigChangeListener listener){
        listeners.add(listener);
    }

    public void removeListener(ConfigChangeListener listener){
        listeners.remove(listener);
    }

    public void notifyListeners(){
        for(ConfigChangeListener listener : listeners){
            listener.onConfigChanged(this);
        }
    }

    public boolean isLimitEnable() {
        return isLimitEnable;
    }

    public void setLimitEnable(boolean limitEnable) {
        isLimitEnable = limitEnable;
        fileConfiguration.set("enable_limit", limitEnable);
        notifyListeners();
    }

    public Long getIpExpireMin() {
        return ipExpireMin;
    }

    public Long getIpClearMin() {
        return ipClearMin;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
        notifyListeners();
    }

    public Integer getMaxAccount() {
        return maxAccount;
    }

    public void setMaxAccount(int maxAccount) {
        this.maxAccount = maxAccount;
        notifyListeners();
    }
}
