package org.zzq.iPAccountDetection.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CommandCompleter implements TabCompleter {
    public static final String adminPermission = "ipaccountdetection.admin";
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // 第一个参数：主命令补全
            return getMainCompletions(sender, args[0]);
        } else if (args.length == 2) {
            String firstArg = args[0];
            if(firstArg.equals("add") && sender.hasPermission(adminPermission)){
                completions.addAll(addOnlinePlayer(sender, args[1]));
            } else if (firstArg.equals("remove") && sender.hasPermission(adminPermission)) {
                completions.addAll(addOnlinePlayer(sender, args[1]));
            } else if (firstArg.equals("view") && sender.hasPermission(adminPermission)) {
                completions.addAll(addOnlinePlayer(sender, args[1]));
            }else if (firstArg.equals("setlimit") && sender.hasPermission(adminPermission)) {
                completions.addAll(addOnlinePlayer(sender, args[1]));
            }else if (firstArg.equals("limitedaccount") && sender.hasPermission(adminPermission)) {
                completions.addAll(addLimitedAccountCompletions(args[1]));
            }else if (firstArg.equals("setmain") && sender.hasPermission(adminPermission)) {
                completions.addAll(addOnlinePlayer(sender, args[1]));
            }else if (firstArg.equals("setlimitenable") && sender.hasPermission(adminPermission)) {
                completions.addAll(addBoolean(sender, args[1]));
            }else if (firstArg.equals("luckperms") && sender.hasPermission(adminPermission)) {
                completions.addAll(addBoolean(sender, args[1]));
            }else if (firstArg.equals("setban") && sender.hasPermission(adminPermission)) {
                completions.addAll(addOnlinePlayer(sender, args[1]));
            }
        }else if (args.length == 3) {
            String firstArg = args[0];
            String secondArg = args[1];
            if(firstArg.equals("limitedaccount") && (secondArg.equals("add") || secondArg.equals("remove")) && sender.hasPermission(adminPermission)){
                completions.addAll(addOnlinePlayer(sender, args[2]));
            } else if (firstArg.equals("limitedaccount") && sender.hasPermission(adminPermission)) {
                completions.addAll(addOnlinePlayer(sender, args[2]));
            } else if (firstArg.equals("setban") && sender.hasPermission(adminPermission)) {
                completions.addAll(addBoolean(sender, args[2]));
            }
        }

        return completions;
    }
    private List<String> getMainCompletions(CommandSender sender, String currentArg) {
        List<String> availableCommands = new ArrayList<>();

        // 基础权限检查
        if (sender.hasPermission("ipaccountdetection.use")) {
            availableCommands.add("view");

        }

        // 管理员权限检查
        if (sender.hasPermission("ipaccountdetection.admin")) {
            availableCommands.add("setlimit");
            availableCommands.add("remove");
            availableCommands.add("reload");
            availableCommands.add("add");
            availableCommands.add("stats");
            availableCommands.add("limitedaccount");
            availableCommands.add("removeip");
            availableCommands.add("setmain");
            availableCommands.add("setlimitenable");
            availableCommands.add("luckperms");
            availableCommands.add("setban");
        }

        // 使用 StringUtil 匹配部分输入
        return StringUtil.copyPartialMatches(
                currentArg,
                availableCommands,
                new ArrayList<>()
        );
    }

    private List<String> addOnlinePlayer(CommandSender sender, String currentArg){
        List<String> playerArg = new ArrayList<>(sender.getServer().getOnlinePlayers().stream().map(Player::getName).toList());
        return StringUtil.copyPartialMatches(currentArg, playerArg, new ArrayList<>());
    }

    private List<String> addLimitedAccountCompletions(String currentArg){
        List<String> playerArg = List.of("add", "remove", "list");
        return StringUtil.copyPartialMatches(currentArg, playerArg, new ArrayList<>());
    }

    private List<String> addBoolean(CommandSender sender, String currentArg){
        List<String> booleanArg = new ArrayList<>(List.of("true", "false"));
        return StringUtil.copyPartialMatches(currentArg, booleanArg, new ArrayList<>());
    }
}
