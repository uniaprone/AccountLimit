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

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
        notifyListeners();
    }
}
