package org.zzq.iPAccountDetection;

import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.zzq.iPAccountDetection.Listener.PlayerCommandListener;
import org.zzq.iPAccountDetection.Listener.PlayerLoginListener;
import org.zzq.iPAccountDetection.Listener.PlayerMoveListener;
import org.zzq.iPAccountDetection.Listener.PlayerQuitListener;
import org.zzq.iPAccountDetection.infrastructure.AccountLogger;
import org.zzq.iPAccountDetection.infrastructure.ConfigManager;
import org.zzq.iPAccountDetection.Manager.DatabaseManager;
import org.zzq.iPAccountDetection.Manager.GroupManager;
import org.zzq.iPAccountDetection.Service.DetectionService;
import org.zzq.iPAccountDetection.model.LimitAccount;
import org.zzq.iPAccountDetection.Service.LimitService;
import org.zzq.iPAccountDetection.command.CommandCompleter;
import org.zzq.iPAccountDetection.command.IPCheckCommand;

import java.util.Objects;

public class AccountLimit extends JavaPlugin implements Listener {
    private ConfigManager configManager;
    private GroupManager groupManager;
    private DetectionService detectionService;
    private LimitAccount limitAccount;
    private LimitService limitService;
    private DatabaseManager databaseManager;
    private AccountLogger accountLogger;

    @Override
    public void onEnable() {
        limitAccount = new LimitAccount();
        limitService = new LimitService(limitAccount);
        configManager = new ConfigManager(this);
        accountLogger = new AccountLogger(this.getLogger(), configManager);
        databaseManager = new DatabaseManager(this);
        groupManager = new GroupManager(this, databaseManager);

        // 初始化组管理器
        detectionService = new DetectionService(accountLogger, groupManager, limitAccount);


        // 注册事件监听器
        eventInitialize();

        // 注册命令
        Objects.requireNonNull(getCommand("ipcheck")).setExecutor(new IPCheckCommand(groupManager,configManager, limitAccount));
        getCommand("ipcheck").setTabCompleter(new CommandCompleter());

        getLogger().info("IP和账号关联检测插件已启用！a");
    }

    @Override
    public void onDisable() {
        // 保存数据
        groupManager.shutdown();
        getLogger().info("IP和账号关联检测插件已禁用！");
    }

    private void eventInitialize(){
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerLoginListener(detectionService, this), this);
        pluginManager.registerEvents(new PlayerCommandListener(limitService), this);
        pluginManager.registerEvents(new PlayerMoveListener(limitService), this);
        pluginManager.registerEvents(new PlayerQuitListener(limitService), this);
    }
}