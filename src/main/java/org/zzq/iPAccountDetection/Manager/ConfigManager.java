package org.zzq.iPAccountDetection.Manager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class ConfigManager {
    private Plugin plugin;
    private File file;
    private FileConfiguration fileConfiguration;

    private int maxAccount;
    public ConfigManager(Plugin plugin){
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig(){
        file = new File(plugin.getDataFolder(), "config.yml");
        if(!file.exists()){
            plugin.saveResource(file.getName(), false);
        }
        fileConfiguration = YamlConfiguration.loadConfiguration(file);
        parseConfig();
    }

    public void parseConfig(){
        maxAccount = fileConfiguration.getInt("max-accounts-per-group", 3);
    }

    public int getMaxAccount() {
        return maxAccount;
    }

    public void setMaxAccount(int maxAccounts) {
        this.maxAccount = maxAccounts;
        fileConfiguration.set("max-accounts-per-group", maxAccounts);
        plugin.saveConfig();
    }
}
