package org.zzq.iPAccountDetection.infrastructure;

import org.zzq.iPAccountDetection.model.aggregate.Group;
import org.zzq.iPAccountDetection.model.entity.Account;
import org.zzq.iPAccountDetection.model.entity.IP;
import org.zzq.iPAccountDetection.model.repository.IMemoryGroupRepository;


import java.util.*;

public class MemoryGroupRepository implements IMemoryGroupRepository {
    private final GroupDatabase groupDatabase;
    private Map<String, Group> groupMap;
    private Map<String, String> accountMap;
    private Map<String, String> ipMap;
    private LogUtil logger;

    public MemoryGroupRepository(GroupDatabase groupDatabase, LogUtil logger) {
        this.groupDatabase = groupDatabase;
        GroupLoadResult groupLoadResult= groupDatabase.loadGroupsWithAssociations();
        this.groupMap = groupLoadResult.getGroupMap();
        this.accountMap = groupLoadResult.getAccountToGroupMap();
        this.ipMap = groupLoadResult.getIpToGroupMap();
        this.logger = logger;
    }

    @Override
    public Map<String, Group> getGroups() {
        return groupMap;
    }

    @Override
    public boolean setMaxAccount(String groupId, int count) {
        if(!groupMap.containsKey(groupId)){
            logger.error("设置最大账号数量时: " + groupId + "时组不存在");
            return false;
        }
        Group group = groupMap.get(groupId);
        if(group == null){
            logger.error("设置最大账号数量时: " + groupId + "时组为空");
            return false;
        }
        group.setMaxAccount(count);
        return true;
    }

    @Override
    public boolean setBanGroup(String groupId, boolean isBan) {
        if(!groupMap.containsKey(groupId)){
            logger.error("设置封禁账号时: " + groupId + "组不存在");
            return false;
        }
        Group group = groupMap.get(groupId);
        if(group == null){
            logger.error("设置封禁账号时: " + groupId + "组为空");
            return false;
        }
        group.setBan(isBan);
        return true;
    }

    public boolean addGroup(Group group){
        if(groupMap.containsKey(group.getGroupId())){
            logger.error("向内存添加组groupId: " + group.getGroupId() + "时组已存在");
            return false;
        }
        groupMap.put(group.getGroupId(), group);
        accountMap.put(group.getAccounts().getFirst().getAccountId(), group.getGroupId());
        ipMap.put(group.getIps().getFirst().getIp(), group.getGroupId());
        return true;
    }

    public boolean removeGroup(String groupId){
        if(!groupMap.containsKey(groupId)){
            logger.error("移除组groupId: " + groupId + "时groupMap这中不存在groupId");
            return false;
        }
        Group group = groupMap.get(groupId);
        if(group == null){
            logger.error("移除组groupId: " + groupId + "时group为空");
            return false;
        }
        List<String> removedAccounts = group.getAccounts().stream().map(Account::getAccountId).toList();
        removedAccounts.forEach(accountId -> {accountMap.remove(accountId);});
        logger.info("移除组中" + removedAccounts.size() + "个账号" + String.join(",", removedAccounts));
        List<String> removedIPs = group.getIps().stream().map(IP::getIp).toList();
        removedIPs.forEach(ip -> {ipMap.remove(ip);});
        logger.info("移除组中" + removedIPs.size() + "个ip" + String.join(",", removedIPs));
        groupMap.remove(groupId);
        return true;
    }

    @Override
    public String getIPGroupId(String ip) {
        return ipMap.get(ip);
    }

    @Override
    public String getAccountGroupId(String accountId) {
        return accountMap.get(accountId);
    }

    @Override
    public boolean setMainAccount(String groupId, String accountId) {
        if(!groupMap.containsKey(groupId)){
            logger.error("设置主账号时 " + groupId + " 不在groupMap中");
            return false;
        }
        Group group = groupMap.get(groupId);
        if(group == null){
            logger.error("设置主账号时 " + groupId + " groupMap中值为空");
            return false;
        }
        group.setMainAccount(accountId);
        return true;
    }

    @Override
    public Group getGroupById(String groupId) {
        if(!groupMap.containsKey(groupId)){
            logger.error("通过组id查找组时 " + groupId + " 组不存在");
            return null;
        }
        Group group = groupMap.get(groupId);
        if(group == null){
            logger.error("通过组id查找组时 " + groupId + " 组为空");
            return null;
        }
        return group;
    }

    public boolean addGroupIP(String groupId, String ip, long time){
        if(!groupMap.containsKey(groupId)){
            logger.error("向组groupId: " + groupId + "添加ip： " + ip +  "时组不存在groupId");
            return false;
        }
        Group group = groupMap.get(groupId);
        if(group == null){
            logger.error("向组groupId: " + groupId + "添加ip： " + ip +  "时组为空");
            return false;
        }
        group.addIPs(new IP(groupId, ip, time));
        ipMap.put(ip, groupId);
        return true;
    }

    public boolean addGroupAccount(String groupId, String accountId, String accountName, boolean isMain){
        if(!groupMap.containsKey(groupId)){
            logger.error("向组groupId: " + groupId + "添加账号： " + accountId + "账号名： " + accountName + "时组不存在groupId");
            return false;
        }
        Group group = groupMap.get(groupId);
        if(group == null){
            logger.error("向组groupId: " + groupId + "添加账号： " + accountId + "账号名： " + accountName + "时组为空");
            return false;
        }
        group.addAccounts(new Account(groupId, accountId, accountName, isMain));
        accountMap.put(accountId, groupId);
        return true;
    }

    @Override
    public boolean removeGroupAccount(String accountId) {
        String groupId = accountMap.get(accountId);
        if(groupId == null){
            logger.error("移除账号: " + accountId + "时找不到组");
            return false;
        }
        if(!groupMap.containsKey(groupId)){
            logger.error("移除账号: " + accountId + "时组: " + groupId + "不存在表中");
            return false;
        }
        Group group = groupMap.get(groupId);
        if(groupMap == null){
            logger.error("移除账号: " + accountId + "时组: " + groupId + " 为空");
            return false;
        }
        accountMap.remove(accountId);
        group.removeAccountById(accountId);
        return true;
    }

    @Override
    public boolean removeGroupIP(String ipAddress) {
        String groupId = ipMap.get(ipAddress);
        if(groupId == null){
            logger.error("移除ip: " + ipAddress + " 时找不到组");
            return false;
        }
        if(!groupMap.containsKey(groupId)){
            logger.error("移除ip: " + ipAddress + "时组: " + groupId + "不存在表中");
            return false;
        }
        Group group = groupMap.get(groupId);
        if(groupMap == null){
            logger.error("移除ip: " + ipAddress + "时组: " + groupId + " 为空");
            return false;
        }
        ipMap.remove(ipAddress);
        group.removeIP(ipAddress);
        return true;
    }

    @Override
    public void reload() {
        GroupLoadResult groupLoadResult= groupDatabase.loadGroupsWithAssociations();
        this.groupMap = groupLoadResult.getGroupMap();
        this.accountMap = groupLoadResult.getAccountToGroupMap();
        this.ipMap = groupLoadResult.getIpToGroupMap();
    }


    @Override
    public Group getGroupByIP(String ip) {
        String groupId = ipMap.get(ip);
        if(groupId == null){
            logger.error("通过ip获取组时不存在 " + ip);
            return null;
        }
        if(!groupMap.containsKey(groupId)){
            logger.error("通过ip获取组时ip表存在组表不存在 ip： " + ip + " groupId: " + groupId);
            return null;
        }
        Group group = groupMap.get(groupId);
        if(group == null){
            logger.error("通过ip获取组时获取的组为空： " + ip + " groupId: " + groupId);
            return null;
        }
        return group;
    }

    @Override
    public Group getGroupByAccountId(String accountId) {
        String groupId = accountMap.get(accountId);
        if(groupId == null){
            logger.info("通过accountId获取组时不存在 " + accountId);
            return null;
        }
        if(!groupMap.containsKey(groupId)){
            logger.error("通过accountId获取组时accountId表存在组表不存在 accountId： " + accountId + " groupId: " + groupId);
            return null;
        }
        Group group = groupMap.get(groupId);
        if(group == null){
            logger.error("通过accountId获取组时获取的组为空： " + accountId + " groupId: " + groupId);
            return null;
        }
        return group;
    }
    @Override
    public boolean updateIPLastLogin(String groupId, String ip, long time){
        if(!groupMap.containsKey(groupId)){
            logger.error("向组groupId: " + groupId + "更新ip： " + ip +  "登录时间时组不存在groupId");
            return false;
        }
        Group group = groupMap.get(groupId);
        if(group == null){
            logger.error("向组groupId: " + groupId + "更新ip： " + ip +  "登录时间时组为空");
            return false;
        }
        group.updateIPLoginTime(ip, time);
        return true;
    }
    @Override
    public List<IP> removeExpireIP(long currentTime, long expireTime, Set<String> onlineIPs){
        List<IP> allRemovedIP = new ArrayList<>();
        for(Map.Entry<String, Group> groupEntry: getGroups().entrySet()){
            Group group = groupEntry.getValue();
            List<IP> expireIPs = new ArrayList<>(group.removeExpireIPs(currentTime, expireTime));
            expireIPs.removeIf(ip -> onlineIPs.contains(ip.getIp()));
            if (!expireIPs.isEmpty()) {
                allRemovedIP.addAll(expireIPs);
            }
        }
        for(IP ip: allRemovedIP){
            ipMap.remove(ip.getIp());
        }
        return allRemovedIP;
    }
}
