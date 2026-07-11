package org.zzq.iPAccountDetection.infrastructure;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    private Plugin plugin;
    private LogUtil logger;
    private File file;
    private FileConfiguration fileConfiguration;
    private final List<ConfigChangeListener> listeners = new ArrayList<>();

    private String env;
    private Integer maxAccount;
    private Long ipExpireMin;
    private Long ipClearMin;
    private boolean isLuckPermsEnable;
    private String luckpermsSecondaryAccountGroupName;

    private LimitEventConfig limitEventConfig;

    public ConfigManager(Plugin plugin, LogUtil logger){
        this.plugin = plugin;
        this.logger = logger;
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
        isLuckPermsEnable = fileConfiguration.getBoolean("luckperms_enable", false);
        luckpermsSecondaryAccountGroupName = fileConfiguration.getString("luckperms_secondary_account_group_name", "secondary");

        limitEventConfig = new LimitEventConfig(
                fileConfiguration.getBoolean("limit_event.enable", false),
                fileConfiguration.getInt("limit_event.trigger_interval_times", 10),
                parseLimitTitleConfig(),
                parseLimitMessageConfig()
        );

        notifyListeners();
    }

    private LimitTitleConfig parseLimitTitleConfig() {
        return new LimitTitleConfig(
                fileConfiguration.getBoolean("limit_event.limit_title.enable", false),
                fileConfiguration.getInt("limit_event.limit_title.fadein_ticks", 20),
                fileConfiguration.getInt("limit_event.limit_title.stay_ticks", 100),
                fileConfiguration.getInt("limit_event.limit_title.fadeout_ticks", 20),
                fileConfiguration.getString("limit_event.limit_title.title.message", "该账号状态异常"),
                fileConfiguration.getString("limit_event.limit_title.title.color", "yellow"),
                fileConfiguration.getString("limit_event.limit_title.sub_title.message", "如有疑问请加群反馈!"),
                fileConfiguration.getString("limit_event.limit_title.sub_title.color", "YELLOW")
        );
    }

    private LimitMessageConfig parseLimitMessageConfig() {
        return new LimitMessageConfig(
                fileConfiguration.getBoolean("limit_event.limit_message.enable", false),
                fileConfiguration.getStringList("limit_event.limit_message.messages")
        );
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

    public void setLimitEnable(boolean limitEnable) {
        limitEventConfig.setEnable(limitEnable);
        fileConfiguration.set("limit_event.enable", limitEnable);
        try {
            fileConfiguration.save(file); // 保存到文件
        } catch (IOException e) {
            logger.warn("无法保存配置文件" + e);
        }
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

    public boolean isLuckPermsEnable() {
        return isLuckPermsEnable;
    }

    public void setLuckPermsEnable(boolean luckPermsEnable) {
        isLuckPermsEnable = luckPermsEnable;
        fileConfiguration.set("luckperms_enable", luckPermsEnable);
        try {
            fileConfiguration.save(file); // 保存到文件
        } catch (IOException e) {
            logger.warn("无法保存配置文件" + e);
        }
        notifyListeners();
    }

    public LimitEventConfig getLimitDisplayConfig() {
        return limitEventConfig;
    }

    public String getLuckPermsSecondaryAccountGroupName() {
        return luckpermsSecondaryAccountGroupName;
    }
}
