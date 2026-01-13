package org.zzq.iPAccountDetection.infrastructure;

import org.zzq.iPAccountDetection.model.repository.ILimitedAccountRepository;

import java.util.HashMap;
import java.util.Map;

public class LimitedAccountRepository implements ILimitedAccountRepository {
    private Map<String, String> limitedAccounts = new HashMap<>();

    public void addLimitedAccount(String accountId, String accountName){
        limitedAccounts.put(accountId, accountName);
    }

    public void removeLimitedAccount(String accountId){
        limitedAccounts.remove(accountId);
    }

    public Map<String, String> getLimitedAccounts(){
        return new HashMap<>(limitedAccounts);
    }

    public void clearLimitedAccounts(){
        limitedAccounts.clear();
    }

    @Override
    public boolean isLimitedAccount(String accountId) {
        return limitedAccounts.containsKey(accountId);
    }
}
