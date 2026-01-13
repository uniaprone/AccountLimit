package org.zzq.iPAccountDetection.model.repository;

import org.zzq.iPAccountDetection.infrastructure.GroupLoadResult;
import org.zzq.iPAccountDetection.model.entity.Account;
import org.zzq.iPAccountDetection.model.aggregate.Group;
import org.zzq.iPAccountDetection.model.entity.IP;

import java.util.List;
import java.util.Map;

public interface IGroupRepository {
    GroupLoadResult loadGroupsWithAssociations();
    boolean insertOrReplaceGroup(String groupId, int count);
    boolean deleteGroup(String groupId);
    boolean addAccount(String accountId, String accountName, String groupId, boolean isMain);
    boolean setMainAccount(String accountId, String groupId);
    boolean deleteAccount(String accountId);
    boolean insertOrUpdateIP(String ip, String groupId, long time);
    boolean cleanupExpiredIPs(List<IP> expiredIPs);
    boolean deleteIP(String ip);
    boolean addGroup(String groupId, int maxAccount, String accountId, String accountName, boolean isMain, String ips, long lastLogin);
}
