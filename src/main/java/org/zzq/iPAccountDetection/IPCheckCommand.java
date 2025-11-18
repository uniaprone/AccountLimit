package org.zzq.iPAccountDetection;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class IPCheckCommand implements CommandExecutor, TabCompleter {
    private final DetectionService detectionService;
    private final GroupManager groupManager;
    private final ConfigManager configManager;
    private final List<String> mainCommands = List.of("view", "setlimit", "stats");

    public IPCheckCommand(DetectionService detectionService, GroupManager groupManager, ConfigManager configManager) {
        this.detectionService = detectionService;
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
            case "stats":
                handleStatsCommand(sender);
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
        sender.sendMessage("§e关联账号数: §7" + group.getAccounts().size());
        sender.sendMessage("§e关联IP数: §7" + group.getIPs().size());

        sender.sendMessage("§e关联账号: §7" + String.join(", ", group.getAccounts()));
        sender.sendMessage("§e关联IP: §7" + String.join(", ", group.getIPs()));
    }

    private void handleSetLimitCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§c用法: /ipcheck setlimit <数量>");
            return;
        }

        try {
            int limit = Integer.parseInt(args[1]);
            if (limit < 1) {
                sender.sendMessage("§c限制值必须大于0");
                return;
            }

            configManager.setMaxAccount(limit);
            sender.sendMessage("§a已设置最大账号限制为: " + limit);
        } catch (NumberFormatException e) {
            sender.sendMessage("§c请输入有效的数字");
        }
    }

    private void handleStatsCommand(CommandSender sender) {
        sender.sendMessage("§6=== IP账号检测统计 ===");
        sender.sendMessage("§e总组数: §7" + groupManager.getTotalGroups());
        sender.sendMessage("§e当前最大账号限制: §7" + configManager.getMaxAccount());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // 第一个参数：主命令补全
            return getMainCompletions(sender, args[0]);
        }

        switch (args[0].toLowerCase()) {
            case "view":
                return getViewCompletions(sender, args[1]);
        }

        return completions;
    }

    private List<String> getMainCompletions(CommandSender sender, String currentArg) {
        List<String> availableCommands = new ArrayList<>();

        // 基础权限检查
        if (sender.hasPermission("ipaccountdetection.use")) {
            availableCommands.add("view");
            availableCommands.add("stats");
        }

        // 管理员权限检查
        if (sender.hasPermission("ipaccountdetection.admin")) {
            availableCommands.add("setlimit");
        }

        // 使用 StringUtil 匹配部分输入
        return StringUtil.copyPartialMatches(
                currentArg,
                availableCommands,
                new ArrayList<>()
        );
    }

    private List<String> getViewCompletions(CommandSender sender, String currentArg){
        List<String> playerArg = new ArrayList<>();
        if(!sender.hasPermission("ipaccountdetection.admin")) return  playerArg;
        playerArg.addAll(sender.getServer().getOnlinePlayers().stream().map(Player::getName).toList());
        return StringUtil.copyPartialMatches(currentArg, playerArg, new ArrayList<>());
    }
}