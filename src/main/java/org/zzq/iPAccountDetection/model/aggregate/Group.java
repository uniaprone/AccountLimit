package org.zzq.iPAccountDetection.model.aggregate;

import org.zzq.iPAccountDetection.model.entity.Account;
import org.zzq.iPAccountDetection.model.entity.IP;

import java.util.*;
import java.util.stream.Collectors;

public class Group {
    private String groupId;
    private int maxAccount;
    private boolean isBan;
    private List<Account> accounts = new ArrayList<>();
    private List<IP> ips = new ArrayList<>();

    public Group(String groupId, int maxAccount) {
        this.groupId = groupId;
        this.maxAccount = maxAccount;
        this.accounts = new ArrayList<>();
        this.ips = new ArrayList<>();
        this.isBan = false;
    }

    public Group(String groupId, int maxAccount, ArrayList<Account> accounts, ArrayList<IP> ips, boolean is_ban) {
        this.groupId = groupId;
        this.maxAccount = maxAccount;
        this.accounts = accounts;
        this.ips = ips;
        this.isBan = is_ban;
    }

    public Group() {}

    public static Group createGroup(String accountId, String accountName, String ipAddress, long time) {
        Group group = new Group();
        String groupId = UUID.randomUUID().toString();
        group.setGroupId(groupId);
        group.setMaxAccount(2);
        group.setBan(false);
        Account account = new Account(groupId, accountId, accountName, true);
        group.setAccounts(new ArrayList<>(List.of(account)));
        IP ip = new IP(groupId, ipAddress, time);
        group.setIps(new ArrayList<>(List.of(ip)));
        return group;
    }

    public boolean isBan() {
        return isBan;
    }

    public void setBan(boolean ban) {
        isBan = ban;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public int getMaxAccount() {
        return maxAccount;
    }

    public void setMaxAccount(int maxAccount) {
        this.maxAccount = maxAccount;
    }

    public List<Account> getAccounts(){
        return new ArrayList<>(accounts);
    }

    public String getMainAccount(){
        if(accounts == null) return "";
        return accounts.stream()
                .filter(Account::isMain)
                .findFirst()
                .map(Account::getAccountName)
                .orElse("");
    }

    public boolean setMainAccount(String accountId){
        if(accounts == null) return false;
        accounts.stream()
                .filter(Account::isMain)
                .forEach(acc -> acc.setMain(false));

        return accounts.stream()
                .filter(acc -> Objects.equals(acc.getAccountId(), accountId))
                .findFirst()
                .map(acc -> {
                    acc.setMain(true);
                    return true;
                })
                .orElse(false);
    }

    public boolean isMainAccount(String accountId){
        if(accounts == null) return false;
        return accounts.stream().anyMatch(acc -> Objects.equals(acc.getAccountId(), accountId) && acc.isMain());
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public void addAccounts(Account account){
        accounts.add(account);
    }

    public boolean hasMainAccount(){
        if(accounts == null) return false;
        return accounts.stream().anyMatch(Account::isMain);
    }

    public boolean canAddAccount(){
        return maxAccount > accounts.size();
    }

    public void removeAccountById(String accountId){
        Iterator<Account> iterator = accounts.iterator();
        while (iterator.hasNext()){
            Account account = iterator.next();
            if(Objects.equals(account.getAccountId(), accountId)){
                iterator.remove();
                break;
            }
        }
    }

    public List<IP> getIps(){
        return new ArrayList<>(ips);
    }

    public void setIps(List<IP> ips) {
        this.ips = ips;
    }

    public void addIPs(IP ip){
        ips.add(ip);
    }

    public void removeIP(String ipAddress){
        Iterator<IP> iterator = ips.iterator();
        while (iterator.hasNext()){
            IP ip = iterator.next();
            if(Objects.equals(ip.getIp(), ipAddress)){
                iterator.remove();
                break;
            }
        }
    }

    public void updateIPLoginTime(String ipAddress, long time){
        for(IP ip: ips){
            if(Objects.equals(ipAddress, ip.getIp())){
                ip.setLastLogin(time);
                return;
            }
        }
    }

    public List<IP> removeExpireIPs(long currentTime, long expireTime) {
        if (ips == null) {
            return new ArrayList<>();
        }

        List<IP> removedIPs = ips.stream()
                .filter(ip -> ip.isExpireIP(currentTime, expireTime))
                .collect(Collectors.toList());

        ips.removeAll(removedIPs);
        return removedIPs;
    }
}
