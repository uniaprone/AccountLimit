package org.zzq.iPAccountDetection.infrastructure;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import org.zzq.iPAccountDetection.model.ILuckpermsExpansion;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class LuckpermsExpansion implements ILuckpermsExpansion {
    private final LuckPerms luckperms;
    private final LogUtil logger;

    public LuckpermsExpansion(LuckPerms luckperms, LogUtil logger) {
        this.luckperms = luckperms;
        this.logger = logger;
    }

    @Override
    public boolean isPlayerInGroup(String playerId, String group) {
        try {
            UUID uuid = UUID.fromString(playerId);
            User user = luckperms.getUserManager().getUser(uuid);

            if (user != null) {
                return user.getNodes().stream()
                        .anyMatch(node -> node.getKey().equals("group." + group));
            } else {
                return luckperms.getUserManager().loadUser(uuid)
                        .thenApply(loadedUser -> loadedUser != null &&
                                loadedUser.getNodes().stream()
                                        .anyMatch(node -> node.getKey().equals("group." + group)))
                        .get(5, TimeUnit.SECONDS);
            }
        } catch (IllegalArgumentException e) {
            logger.warn("无效的UUID格式: " + playerId);
            return false;
        } catch (Exception e) {
            logger.error("检查玩家组关系时出错: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void addPlayerToGroup(String playerId, String groupName) {
        executeGroupOperation(playerId, groupName, true);
    }

    @Override
    public void removePlayerFromGroup(String playerId, String groupName) {
        executeGroupOperation(playerId, groupName, false);
    }

    private void executeGroupOperation(String playerId, String groupName, boolean isAdd) {
        try {
            UUID uuid = UUID.fromString(playerId);
            UserManager userManager = luckperms.getUserManager();

            CompletableFuture<User> userFuture = userManager.loadUser(uuid);
            userFuture.thenAcceptAsync(user -> {
                if (user == null) {
                    logger.warn("无法加载玩家数据: " + playerId);
                    return;
                }

                Node node = Node.builder("group." + groupName).build();
                String operation = isAdd ? "添加" : "移除";

                try {
                    if (isAdd) {
                        user.data().add(node);
                    } else {
                        user.data().remove(node);
                    }

                    userManager.saveUser(user).thenRun(() -> {
                        logger.info("成功" + operation + "玩家 " + playerId + " 到组 " + groupName);
                    }).exceptionally(saveError -> {
                        logger.error(operation + "玩家到组时保存失败: " + saveError.getMessage());
                        return null;
                    });
                } catch (Exception e) {
                    logger.error(operation + "玩家到组时发生错误: " + e.getMessage());
                }
            }).exceptionally(loadError -> {
                logger.error("加载玩家数据时出错: " + loadError.getMessage());
                return null;
            });

        } catch (IllegalArgumentException e) {
            logger.warn("无效的UUID格式: " + playerId);
        }
    }
}

