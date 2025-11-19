package org.zzq.iPAccountDetection.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LimitDataService {
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
}
