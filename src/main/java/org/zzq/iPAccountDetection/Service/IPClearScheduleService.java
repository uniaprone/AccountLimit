package org.zzq.iPAccountDetection.Service;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.zzq.iPAccountDetection.infrastructure.ConfigChangeListener;
import org.zzq.iPAccountDetection.infrastructure.ConfigManager;
import org.zzq.iPAccountDetection.infrastructure.LogUtil;
import org.zzq.iPAccountDetection.model.entity.IP;
import org.zzq.iPAccountDetection.model.repository.IGroupRepository;
import org.zzq.iPAccountDetection.model.repository.IMemoryGroupRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class IPClearScheduleService implements ConfigChangeListener {
    private Plugin plugin;
    private Long ipExpireMinute;
    private Long ipExpireTimeMillis;
    private Long ipClearIntervalMinutes;
    private Long scheduleIntervalTicks;
    private IMemoryGroupRepository memoryGroupRepository;
    private IGroupRepository groupRepository;
    private LogUtil logUtil;

    public IPClearScheduleService(Plugin plugin, ConfigManager configManager, IMemoryGroupRepository memoryGroupRepository, IGroupRepository groupRepository, LogUtil logUtil) {
        this.plugin = plugin;
        this.ipExpireMinute = configManager.getIpExpireMin();
        this.ipClearIntervalMinutes = configManager.getIpClearMin();
        this.ipExpireTimeMillis = ipExpireMinute * 60 * 1000L;
        this.scheduleIntervalTicks = ipClearIntervalMinutes * 60 * 20L;
        this.memoryGroupRepository = memoryGroupRepository;
        this.groupRepository = groupRepository;
        this.logUtil = logUtil;
        configManager.registerListener(this);
        startClearupTask();
    }

    private void startClearupTask() {
        if (isFolia()) {
            try {
                Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin,
                        scheduledTask -> clearExpireIP(),
                        scheduleIntervalTicks,
                        scheduleIntervalTicks
                );
                logUtil.info("已在Folia服务器上注册IP清理任务，间隔: " + (scheduleIntervalTicks / 1200) + " 分钟");
            } catch (Exception e) {
                logUtil.error("Folia调度器注册失败" + e.getMessage());
            }
        } else {
            Bukkit.getScheduler().runTaskTimer(plugin, this::clearExpireIP,
                    scheduleIntervalTicks, scheduleIntervalTicks);
            logUtil.info("已在传统服务器上注册IP清理任务，间隔: " + (scheduleIntervalTicks / 1200) + " 分钟");
        }
    }

    private void clearExpireIP(){
        logUtil.info("正在执行IP清理任务 " + "任务间隔时间: " + (scheduleIntervalTicks / 1200) + " 分钟" + "IP过期时间: " + ipExpireMinute + "分钟");
        long currentTime = System.currentTimeMillis();
        Set<String> onlineIPs = Bukkit.getOnlinePlayers().stream()
                .map(player -> player.getAddress().getAddress().getHostAddress())
                .collect(Collectors.toSet());
        List<IP> expiredIPs = memoryGroupRepository.removeExpireIP(currentTime, ipExpireTimeMillis, onlineIPs);
        groupRepository.cleanupExpiredIPs(expiredIPs);
        String ipListString = expiredIPs.stream().map(IP::getIp).collect(Collectors.joining(","));
        logUtil.info("IP清理任务结束, 清理了 " + expiredIPs.size() + " 个过期IP " + "清理了IP： " + ipListString);
    }

    private boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onConfigChanged(ConfigManager configManager) {
        this.ipExpireMinute = configManager.getIpExpireMin();
        this.ipClearIntervalMinutes = configManager.getIpClearMin();
        this.ipExpireTimeMillis = ipExpireMinute * 60 * 1000L;
        this.scheduleIntervalTicks = ipClearIntervalMinutes * 60 * 20L;
    }
}
