package org.zzq.iPAccountDetection;

import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zzq.iPAccountDetection.Listener.*;
import org.zzq.iPAccountDetection.Service.IPClearScheduleService;
import org.zzq.iPAccountDetection.Service.LimitService;
import org.zzq.iPAccountDetection.Service.LuckPermsService;
import org.zzq.iPAccountDetection.infrastructure.*;
import org.zzq.iPAccountDetection.Service.DetectionService;
import org.zzq.iPAccountDetection.command.CommandCompleter;
import org.zzq.iPAccountDetection.command.IPCheckCommand;
import org.zzq.iPAccountDetection.model.repository.ILimitedAccountRepository;
import org.zzq.iPAccountDetection.model.repository.IMemoryGroupRepository;

import java.util.Objects;

public class AccountLimit extends JavaPlugin implements Listener {
    private static final Logger log = LoggerFactory.getLogger(AccountLimit.class);
    private ConfigManager configManager;
    private DetectionService detectionService;
    private LuckPermsService luckPermsService;
    private GroupDatabase groupDatabase;
    private LuckpermsExpansion luckpermsExpansion;
    private LogUtil logger;

    private IMemoryGroupRepository memoryGroupRepository;
    private ILimitedAccountRepository limitedAccountRepository;

    private LimitService limitService;

    @Override
    public void onEnable() {
        logger = new LogUtil(this.getLogger(), this.getDataFolder());
        configManager = new ConfigManager(this, logger);
        groupDatabase = new GroupDatabase(this, logger);
        memoryGroupRepository = new MemoryGroupRepository(groupDatabase, logger);
        limitedAccountRepository = new LimitedAccountRepository();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            logger.info("检测到PlaceholderAPI...");
            new AccountExpansion(memoryGroupRepository).register();
        }

        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            logger.info("检测到LuckPerms...");
            luckpermsExpansion = new LuckpermsExpansion(provider.getProvider(), logger);
            luckPermsService = new LuckPermsService(memoryGroupRepository, luckpermsExpansion, configManager, logger);
        }

        detectionService = new DetectionService(memoryGroupRepository, groupDatabase, limitedAccountRepository, "ipcheck.admin", logger);
        limitService = new LimitService(limitedAccountRepository, configManager);
        new IPClearScheduleService(this, configManager, memoryGroupRepository, groupDatabase, logger);


        eventInitialize();

        Objects.requireNonNull(getCommand("ipcheck")).setExecutor(new IPCheckCommand(memoryGroupRepository, configManager, groupDatabase, limitedAccountRepository, luckpermsExpansion));
        getCommand("ipcheck").setTabCompleter(new CommandCompleter());

        getLogger().info("IP和账号关联检测插件已启用！");
    }

    @Override
    public void onDisable() {
        getLogger().info("IP和账号关联检测插件已禁用！");
    }

    private void eventInitialize(){
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerLoginListener(detectionService, luckPermsService), this);
        pluginManager.registerEvents(new PlayerCommandListener(limitService), this);
        pluginManager.registerEvents(new PlayerMoveListener(limitService), this);
        pluginManager.registerEvents(new PlayerQuitListener(limitService), this);
//        if(pluginManager.isPluginEnabled("PlaceholderAPI")){ new AccountExpansion(groupManager);}
    }
}