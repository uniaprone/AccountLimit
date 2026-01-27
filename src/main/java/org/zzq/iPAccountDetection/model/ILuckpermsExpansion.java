package org.zzq.iPAccountDetection.model;

import org.bukkit.entity.Player;

public interface ILuckpermsExpansion {
    boolean isPlayerInGroup(String playerId, String group);
    void addPlayerToGroup(String playerId, String groupName);
    void removePlayerFromGroup(String playerId, String groupName);
}
