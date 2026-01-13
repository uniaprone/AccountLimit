package org.zzq.iPAccountDetection.infrastructure;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.zzq.iPAccountDetection.model.aggregate.Group;
import org.zzq.iPAccountDetection.model.entity.Account;
import org.zzq.iPAccountDetection.model.repository.IMemoryGroupRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AccountExpansion extends PlaceholderExpansion {
    private IMemoryGroupRepository memoryGroupRepository;

    public AccountExpansion(IMemoryGroupRepository memoryGroupRepository) {
        this.memoryGroupRepository = memoryGroupRepository;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "accountmanager";
    }

    @Override
    public @NotNull String getAuthor() {
        return "zzq";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.isEmpty()) return "";

        // 安全获取玩家UUID
        String playerId = player.getUniqueId().toString();

        // 安全查询组信息
        Group group = memoryGroupRepository.getGroupByAccountId(playerId);
        if (group == null) {
            return "未找到组信息"; // 或返回空字符串
        }

        switch (params.toLowerCase()) {
            case "max_account":
                return String.valueOf(group.getMaxAccount());
            case "main_account":
                return String.valueOf(group.getMainAccount());
            case "account_num":
                return String.valueOf(group.getAccounts().size());
            case "accounts":
                List<Account> accounts = group.getAccounts();
                if (accounts == null || accounts.isEmpty()) {
                    return "无账号";
                }
                return accounts.stream()
                        .map(Account::getAccountName)
                        .filter(Objects::nonNull) // 过滤空值
                        .collect(Collectors.joining(" "));
            case "is_main":
                return String.valueOf(group.isMainAccount(playerId));
            default:
                return "none";
        }
    }
}
