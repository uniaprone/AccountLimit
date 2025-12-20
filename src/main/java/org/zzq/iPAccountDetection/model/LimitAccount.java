package org.zzq.iPAccountDetection.model;

import java.util.HashSet;
import java.util.Set;

public class LimitAccount {
    private Set<String> limitAccounts = new HashSet<>();
    public void addLimitAccount(String name){
        limitAccounts.add(name);
    }
    public void removeLimitAccount(String name){
        limitAccounts.remove(name);
    }
    public boolean isLimitAccount(String name){
        return limitAccounts.contains(name);
    }

    public String getLimitedAccounts() {
        return String.join(", ", limitAccounts); // 使用逗号+空格分隔
    }
}
