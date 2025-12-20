package org.zzq.iPAccountDetection.model;

import java.util.HashSet;
import java.util.Set;

public class Group {
    private final String id;
    private int maxAccount;
    private final Set<String> accounts = new HashSet<>();
    private final Set<String> ips = new HashSet<>();

    public Group(String id) {
        this.id = id;
        this.maxAccount = 2;
    }

    public String getId() {
        return id;
    }

    public Set<String> getAccounts() {
        return new HashSet<>(accounts);
    }

    public Set<String> getIPs() {
        return new HashSet<>(ips);
    }

    public void addAccount(String account) {
        accounts.add(account);
    }

    public void addIP(String ip) {
        ips.add(ip);
    }

    public boolean removeAccount(String account) {
        return accounts.remove(account);
    }

    public boolean removeIP(String ip) {
        return ips.remove(ip);
    }

    public int size() {
        return accounts.size();
    }

    public int getMaxAccount() {
        return maxAccount;
    }

    public void setMaxAccount(int maxAccount) {
        this.maxAccount = maxAccount;
    }

    @Override
    public String toString() {
        return "Group{" +
                "id='" + id + '\'' +
                ", maxAccount" + maxAccount +
                ", accounts=" + accounts +
                ", ips=" + ips +
                '}';
    }
}
