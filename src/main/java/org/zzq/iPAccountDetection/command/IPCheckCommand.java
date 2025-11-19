package org.zzq.iPAccountDetection.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.bukkit.command.TabCompleter;
import org.zzq.iPAccountDetection.Manager.ConfigManager;
import org.zzq.iPAccountDetection.Manager.GroupManager;
import org.zzq.iPAccountDetection.Service.DetectionService;
import org.zzq.iPAccountDetection.model.Group;

import java.util.ArrayList;
import java.util.List;

public class IPCheckCommand implements CommandExecutor {
    private final GroupManager groupManager;
    private final ConfigManager configManager;
    public IPCheckCommand(GroupManager groupManager, ConfigManager configManager) {
        this.groupManager = groupManager;
        this.configManager = configManager;
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
            case "reload":
                handleReloadCommand(sender);
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
        Group group = groupManager.getGroupByPlayer(playerName);

        if (group == null) {
            sender.sendMessage("§7未找到玩家 " + playerName + " 的关联信息");
            return;
        }

        sender.sendMessage("§6=== " + playerName + " 的关联信息 ===");
        sender.sendMessage("§e组ID: §7" + group.getId());
        sender.sendMessage("§e最大账号数: §7" + group.getMaxAccount());
        sender.sendMessage("§e关联账号数: §7" + group.getAccounts().size());
        sender.sendMessage("§e关联IP数: §7" + group.getIPs().size());

        sender.sendMessage("§e关联账号: §7" + String.join(", ", group.getAccounts()));
        sender.sendMessage("§e关联IP: §7" + String.join(", ", group.getIPs()));
    }

    private void handleSetLimitCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
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

            if(groupManager.setMaxAccount(player, limit)){
                sender.sendMessage("§a已设置玩家" + player + "最大账号限制为: " + limit);
            }else{
                sender.sendMessage("§c无效账号或数字");
            }

        } catch (NumberFormatException e) {
            sender.sendMessage("§c请输入有效的数字");
        }
    }

    private void handleRemoveCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§c用法: /ipcheck remove <玩家>");
            return;
        }
        String player = args[1];
        if(groupManager.removeAccount(player)){
            sender.sendMessage("已成功删除 " + player);
        }else{
            sender.sendMessage("§c未找到 " + player + " 或输入不合法");
        }
    }

    private void handleStatsCommand(CommandSender sender) {
        sender.sendMessage("§6=== IP账号检测统计 ===");
        sender.sendMessage("§e总组数: §7" + groupManager.getTotalGroups());
    }

    private void handleAddCommand(CommandSender sender, String[] args){
        if(args.length < 2){
            sender.sendMessage("§c用法: /ipcheck add <玩家> <玩家>");
            return;
        }
        String addedAccount = args[1];
        String addAccount = args[2];
        if(groupManager.addAccount(addedAccount, addAccount)){
            sender.sendMessage("已成功添加 " + addAccount + " 到 " + addedAccount);
        }else{
            sender.sendMessage("§c未找到 " + addedAccount + " 或输入不合法");
        }
    }

    private void handleReloadCommand(CommandSender sender){
        groupManager.shutdown();
        groupManager.reload();
        sender.sendMessage("§a重载成功");
    }
}