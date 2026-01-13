package org.zzq.iPAccountDetection.model.repository;

import org.zzq.iPAccountDetection.model.aggregate.Group;
import org.zzq.iPAccountDetection.model.entity.IP;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IMemoryGroupRepository {
    Map<String, Group> getGroups();
    boolean setMaxAccount(String groupId, int count);
    boolean addGroup(Group group);
    boolean removeGroup(String groupId);
    boolean updateIPLastLogin(String groupId, String ipAddress, long time);
    List<IP> removeExpireIP(long currentTime, long expireTime, Set<String> onlineIPs);
    String getIPGroupId(String ip);
    String getAccountGroupId(String accountId);
    boolean setMainAccount(String groupId, String accountId);
    Group getGroupById(String groupId);
    Group getGroupByIP(String ip);
    Group getGroupByAccountId(String accountId);
    boolean addGroupIP(String groupId, String ip, long time);
    boolean addGroupAccount(String groupId, String accountId, String accountName, boolean isMain);
    boolean removeGroupAccount(String accountId);
    boolean removeGroupIP(String ipAddress);
    void reload();
}
