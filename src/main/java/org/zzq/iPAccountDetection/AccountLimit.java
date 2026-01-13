package org.zzq.iPAccountDetection;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.zzq.iPAccountDetection.Listener.PlayerCommandListener;
import org.zzq.iPAccountDetection.Listener.PlayerLoginListener;
import org.zzq.iPAccountDetection.Listener.PlayerMoveListener;
import org.zzq.iPAccountDetection.Listener.PlayerQuitListener;
import org.zzq.iPAccountDetection.Service.IPClearScheduleService;
import org.zzq.iPAccountDetection.Service.LimitService;
import org.zzq.iPAccountDetection.infrastructure.*;
import org.zzq.iPAccountDetection.Service.DetectionService;
import org.zzq.iPAccountDetection.command.CommandCompleter;
import org.zzq.iPAccountDetection.command.IPCheckCommand;
import org.zzq.iPAccountDetection.model.repository.ILimitedAccountRepository;
import org.zzq.iPAccountDetection.model.repository.IMemoryGroupRepository;

import java.util.Objects;

public class AccountLimit extends JavaPlugin implements Listener {
    private ConfigManager configManager;
    private DetectionService detectionService;
    private GroupDatabase groupDatabase;
    private LogUtil logger;

    private IMemoryGroupRepository memoryGroupRepository;
    private ILimitedAccountRepository limitedAccountRepository;

    private LimitService limitService;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        logger = new LogUtil(this.getLogger(), this.getDataFolder());
        groupDatabase = new GroupDatabase(this, logger);
        memoryGroupRepository = new MemoryGroupRepository(groupDatabase, logger);
        limitedAccountRepository = new LimitedAccountRepository();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new AccountExpansion(memoryGroupRepository).register();
        }

        detectionService = new DetectionService(memoryGroupRepository, groupDatabase, limitedAccountRepository, "ipcheck.admin", logger);
        limitService = new LimitService(limitedAccountRepository, configManager);
        IPClearScheduleService ipClearScheduleService = new IPClearScheduleService(this, configManager, memoryGroupRepository, groupDatabase, logger);


        eventInitialize();

        Objects.requireNonNull(getCommand("ipcheck")).setExecutor(new IPCheckCommand(memoryGroupRepository, configManager, groupDatabase, limitedAccountRepository));
        getCommand("ipcheck").setTabCompleter(new CommandCompleter());

        getLogger().info("IP和账号关联检测插件已启用！");
    }

    @Override
    public void onDisable() {
        getLogger().info("IP和账号关联检测插件已禁用！");
    }

    private void eventInitialize(){
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerLoginListener(detectionService, this), this);
        pluginManager.registerEvents(new PlayerCommandListener(limitService), this);
        pluginManager.registerEvents(new PlayerMoveListener(limitService), this);
        pluginManager.registerEvents(new PlayerQuitListener(limitService), this);
//        if(pluginManager.isPluginEnabled("PlaceholderAPI")){ new AccountExpansion(groupManager);}
    }
}