package org.zzq.iPAccountDetection;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.zzq.iPAccountDetection.AlertService.AlertGUIService;
import org.zzq.iPAccountDetection.AlertService.AlertService;
import org.zzq.iPAccountDetection.Listener.PlayerLoginListener;

import java.util.Objects;

public class IPAccountDetection extends JavaPlugin implements Listener {
    private ConfigManager configManager;
    private GroupManager groupManager;
    private DetectionService detectionService;
    private AlertGUIService alertGUIService;
    private AlertService alertService;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        groupManager = new GroupManager(this);
        alertGUIService = new AlertGUIService();
        alertService = new AlertService(alertGUIService, this.getLogger());
        // 初始化组管理器
        detectionService = new DetectionService(this, configManager, groupManager, alertService);


        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new PlayerLoginListener(detectionService, this), this);

        // 注册命令
        Objects.requireNonNull(getCommand("ipcheck")).setExecutor(new IPCheckCommand(detectionService,groupManager,configManager));

        getLogger().info("IP和账号关联检测插件已启用！");
    }

    @Override
    public void onDisable() {
        // 保存数据
        groupManager.saveData();
        getLogger().info("IP和账号关联检测插件已禁用！");
    }
}