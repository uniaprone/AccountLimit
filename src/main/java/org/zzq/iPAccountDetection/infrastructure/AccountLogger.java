package org.zzq.iPAccountDetection.infrastructure;

import java.util.logging.Logger;

public class AccountLogger implements ConfigChangeListener{
    private Logger logger;
    private String env;
    public AccountLogger(Logger logger, ConfigManager configManager){
        this.logger = logger;
        this.env = configManager.getEnv();
        configManager.registerListener(this);
    }

    public void debug(String message){
        if(!env.equalsIgnoreCase("DEBUG")) return;
        logger.info("[DEBUG] " + message);
    }

    @Override
    public void onConfigChanged(ConfigManager configManager) {
        this.env = configManager.getEnv();
    }
}
