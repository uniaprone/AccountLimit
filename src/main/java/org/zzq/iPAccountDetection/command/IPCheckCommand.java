package org.zzq.iPAccountDetection.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.zzq.iPAccountDetection.infrastructure.ConfigManager;
import org.zzq.iPAccountDetection.infrastructure.GroupDatabase;
import org.zzq.iPAccountDetection.model.aggregate.Group;
import org.zzq.iPAccountDetection.model.entity.Account;
import org.zzq.iPAccountDetection.model.entity.IP;
import org.zzq.iPAccountDetection.model.repository.ILimitedAccountRepository;
import org.zzq.iPAccountDetection.model.repository.IMemoryGroupRepository;
import org.zzq.iPAccountDetection.util.TimeUtil;

import java.util.Objects;
import java.util.stream.Collectors;

public class IPCheckCommand implements CommandExecutor {
    private final IMemoryGroupRepository memoryGroupRepository;
    private final ILimitedAccountRepository limitedAccountRepository;
    private final GroupDatabase groupDatabase;
    private final ConfigManager configManager;
    public IPCheckCommand(IMemoryGroupRepository memoryGroupRepository, ConfigManager configManager, GroupDatabase groupDatabase, ILimitedAccountRepository limitedAccountRepository) {
        this.memoryGroupRepository = memoryGroupRepository;
        this.configManager = configManager;
        this.groupDatabase = groupDatabase;
        this.limitedAccountRepository = limitedAccountRepository;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§6IP账号检测插件命令:");
            sender.sendMessage("§e/ipcheck view <玩家> §7- 查看玩家关联信息");
            sender.sendMessage("§e/ipcheck setlimit <数量> §7- 设置最大账号限制");
            sender.sendMessage("§e/ipcheck stats §7- 查看统计信息");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "view":
                handleViewCommand(sender, args);
                break;
            case "setlimit":
                handleSetLimitCommand(sender, args);
                break;
            case "remove":
                handleRemoveCommand(sender, args);
                break;
            case "stats":
                handleStatsCommand(sender);
                break;
            case "add":
                handleAddCommand(sender, args);
                break;
            case "limitedaccount":
                handleLimitedAccountCommand(sender, args);
                break;
            case "reload":
                handleReloadCommand(sender);
                break;
            case "removeip":
                handleRemoveIPCommand(sender, args);
                break;
            case "setmain":
                handleSetMainCommand(sender, args);
                break;
            case "setlimitenable":
                handleSetLimitEnableCommand(sender, args);
                break;
            default:
                sender.sendMessage("§c未知子命令！");
                break;
        }

        return true;
    }

    private void handleViewCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§c用法: /ipcheck view <玩家>");
            return;
        }

        String playerName = args[1];
        String playerId = Bukkit.getOfflinePlayer(playerName).getUniqueId().toString();
        Group group = memoryGroupRepository.getGroupByAccountId(playerId);

        if (group == null) {
            sender.sendMessage("§7未找到玩家 " + playerName + " 的关联信息");
            return;
        }

        sender.sendMessage("§6=== " + playerName + " 的关联信息 ===");
        sender.sendMessage("§e组ID: §7" + group.getGroupId());
        sender.sendMessage("§e最大账号数: §7" + group.getMaxAccount());
        sender.sendMessage("§e关联账号数: §7" + group.getAccounts().size());
        sender.sendMessage("§e关联IP数: §7" + group.getIps().size());

        String accountString = group.getAccounts().stream()
                .filter(Objects::nonNull) // 过滤空账号
                .map(account -> account.isMain() ? "§e主 " + account.getAccountName() + "§r": account.getAccountName())
                .filter(Objects::nonNull) // 过滤空账号名
                .collect(Collectors.joining(", "));
        sender.sendMessage("§e关联账号: §7" + String.join(", ", accountString));

        String ipString = group.getIps().stream()
                .filter(Objects::nonNull)
                .map(ip -> ip.getIp() + " (" + TimeUtil.formatTimeMillis(ip.getLastLogin()) + ")")
                .collect(Collectors.joining("," ));
        sender.sendMessage("§e关联IP: §7" + String.join(", ", ipString));
    }

    private void handleSetLimitCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§c用法: /ipcheck setlimit <玩家> <数量>");
            return;
        }

        try {
            String player = args[1];
            int limit = Integer.parseInt(args[2]);
            if (limit < 1) {
                sender.sendMessage("§c限制值必须大于0");
                return;
            }
            String playerId = Bukkit.getOfflinePlayer(player).getUniqueId().toString();
            String groupId = memoryGroupRepository.getAccountGroupId(playerId);
            boolean memoryOk = memoryGroupRepository.setMaxAccount(groupId, limit);
            boolean dbOk = groupDatabase.insertOrReplaceGroup(groupId, limit);
            if(memoryOk && dbOk){
                sender.sendMessage("§a已设置玩家" + player + "最大账号限制为: " + limit);
            }else{
                sender.sendMessage("§c无效账号或数字");
            }

        } catch (NumberFormatException e) {
            sender.sendMessage("§c请输入有效的数字");
        }
    }

    private void handleRemoveCommand(CommandSender sender, String[] args) {
        // 参数校验
        if (args.length < 2) {
            sender.sendMessage("§c用法: /ipcheck remove <玩家>");
            return; // 添加return，避免后续执行
        }

        String player = args[1];
        String playerId = Bukkit.getOfflinePlayer(player).getUniqueId().toString();
        Group group = memoryGroupRepository.getGroupByAccountId(playerId);

        // 1. 空值检查：确保IP所属的组存在
        if (group == null) {
            sender.sendMessage("§c错误：未找到账号 " + player + " 对应的组。");
            return;
        }

        // 2. 安全检查：确保组内的IP列表有效
        if (group.getAccounts() == null || group.getAccounts().isEmpty()) {
            sender.sendMessage("§c错误：所在组的账号列表为空，数据可能不一致。");
            return;
        }

        boolean memoryOk;
        boolean dbOk;

        // 3. 简化条件判断：明确区分“移除单个IP”和“移除整个组”的场景
        if (group.getAccounts().size() > 1) {
            // 场景：组内有多个IP，只移除指定IP
            memoryOk = memoryGroupRepository.removeGroupAccount(playerId);
            dbOk = groupDatabase.deleteIP(playerId);
            // 使用更明确的消息
            String operationResult = (memoryOk && dbOk) ? "§a已成功从组 " + group.getGroupId() + " 中移除账号 " + player + "。" : "§c移除账号 " + player + " 失败，请检查日志。";
            sender.sendMessage(operationResult);

        } else {
            // 场景：组内只剩最后一个IP（即要移除的这个），移除整个组
            String groupId = group.getGroupId();
            memoryOk = memoryGroupRepository.removeGroup(groupId);
            dbOk = groupDatabase.deleteGroup(groupId);
            // 消息明确说明移除了整个组
            String operationResult = (memoryOk && dbOk) ? "§a组 " + groupId + " 仅剩一个账号 " + player + "，已成功移除整个组。" : "§c移除组 " + groupId + " 失败，请检查日志。";
            sender.sendMessage(operationResult);
        }

        if (memoryOk != dbOk) {
            sender.sendMessage("移除 账号 失败, 内存操作：" + memoryOk + "数据库操作: " + dbOk);
        }
    }

    private void handleStatsCommand(CommandSender sender) {
        sender.sendMessage("§6=== IP账号检测统计 ===");
        sender.sendMessage("§e总组数: §7" + memoryGroupRepository.getGroups().size());
    }

    private void handleAddCommand(CommandSender sender, String[] args){
        if(args.length < 2){
            sender.sendMessage("§c用法: /ipcheck add <玩家> <玩家>");
            return;
        }
        String addedAccount = args[1];
        String addAccount = args[2];
        String addedPlayerId = Bukkit.getOfflinePlayer(addedAccount).getUniqueId().toString();
        String groupId = memoryGroupRepository.getAccountGroupId(addedPlayerId);
        String addPlayerId = Bukkit.getOfflinePlayer(addAccount).getUniqueId().toString();
        boolean memoryOk = memoryGroupRepository.addGroupAccount(groupId, addPlayerId, addAccount, false);
        boolean dbOk = groupDatabase.addAccount(addPlayerId, addAccount, groupId, false);
        if(memoryOk && dbOk){
            sender.sendMessage("已成功添加 " + addAccount + " 到 " + addedAccount);
        }else{
            sender.sendMessage("§c未找到 " + addedAccount + " 或输入不合法");
        }
    }

    private void handleLimitedAccountCommand(CommandSender sender, String[] args){
        if (args.length < 2) {
            sender.sendMessage("§c用法: /ipcheck limitedAccount list");
            sender.sendMessage("§c用法: /ipcheck limitedAccount add/remove <玩家>");
            return;
        }
        String operation = args[1];
        if(operation.equals("list")){
            sender.sendMessage("已限制玩家列表: " + String.join(", ", limitedAccountRepository.getLimitedAccounts().values()));
            return;
        }

        if(args.length < 3){
            sender.sendMessage("§c用法: /ipcheck limitedAccount add/remove <玩家>");
            return;
        }

        String account = args[2];
        String playerId = Bukkit.getOfflinePlayer(account).getUniqueId().toString();
        if(operation.equals("add")){
            limitedAccountRepository.addLimitedAccount(playerId, account);
            sender.sendMessage("已成功添加 " + account + " 到限制列表");
        }else if(operation.equals("remove")){
            limitedAccountRepository.removeLimitedAccount(playerId);
            sender.sendMessage("已成功从限制列表移除 " + account);
        }
    }

    private void handleRemoveIPCommand(CommandSender sender, String[] args){
        // 参数校验
        if (args.length < 2) {
            sender.sendMessage("§c用法: /ipcheck removeip <ip>");
            return; // 添加return，避免后续执行
        }

        String ipToRemove = args[1];
        Group group = memoryGroupRepository.getGroupByIP(ipToRemove);

        // 1. 空值检查：确保IP所属的组存在
        if (group == null) {
            sender.sendMessage("§c错误：未找到IP地址 " + ipToRemove + " 对应的组。");
            return;
        }

        // 2. 安全检查：确保组内的IP列表有效
        if (group.getIps() == null || group.getIps().isEmpty()) {
            sender.sendMessage("§c错误：所在组的IP列表为空，数据可能不一致。");
            return;
        }

        boolean memoryOk;
        boolean dbOk;

        // 3. 简化条件判断：明确区分“移除单个IP”和“移除整个组”的场景
        if (group.getIps().size() > 1) {
            // 场景：组内有多个IP，只移除指定IP
            memoryOk = memoryGroupRepository.removeGroupIP(ipToRemove);
            dbOk = groupDatabase.deleteIP(ipToRemove);
            // 使用更明确的消息
            String operationResult = (memoryOk && dbOk) ? "§a已成功从组 " + group.getGroupId() + " 中移除IP " + ipToRemove + "。" : "§c移除IP " + ipToRemove + " 失败，请检查日志。";
            sender.sendMessage(operationResult);

        } else {
            // 场景：组内只剩最后一个IP（即要移除的这个），移除整个组
            String groupId = group.getGroupId();
            memoryOk = memoryGroupRepository.removeGroup(groupId);
            dbOk = groupDatabase.deleteGroup(groupId);
            // 消息明确说明移除了整个组
            String operationResult = (memoryOk && dbOk) ? "§a组 " + groupId + " 仅剩一个IP " + ipToRemove + "，已成功移除整个组。" : "§c移除组 " + groupId + " 失败，请检查日志。";
            sender.sendMessage(operationResult);
        }

        if (memoryOk != dbOk) {
            sender.sendMessage("移除 ip 失败, 内存操作：" + memoryOk + "数据库操作: " + dbOk);
        }
    }

    private void handleSetMainCommand(CommandSender sender, String[] args){
        if(args.length < 2){
            sender.sendMessage("§c用法: /ipcheck setmain <玩家>");
        }
        String playerName = args[1];
        String playerId = Bukkit.getOfflinePlayer(playerName).getUniqueId().toString();
        String groupId = memoryGroupRepository.getAccountGroupId(playerId);
        boolean memoryOk = memoryGroupRepository.setMainAccount(groupId, playerId);
        boolean dbOk = groupDatabase.setMainAccount(playerId, groupId);
        if(memoryOk && dbOk){
            sender.sendMessage("已成功设置主账号 " + playerName);
        }else{
            sender.sendMessage("设置主账号 " + playerName + "失败");
        }

    }

    private void handleReloadCommand(CommandSender sender){
        memoryGroupRepository.reload();
        configManager.reload();
        sender.sendMessage("§a重载成功");
    }

    private void handleSetLimitEnableCommand(CommandSender sender, String[] args){
        if(args.length < 2){
            sender.sendMessage("§c用法: /ipcheck setlimitenable true/false");
        }
        boolean enable = Boolean.parseBoolean(args[1]);
        configManager.setLimitEnable(enable);
        sender.sendMessage("§a重载成功");
    }
}