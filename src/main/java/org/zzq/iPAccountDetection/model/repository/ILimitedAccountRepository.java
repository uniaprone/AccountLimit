package org.zzq.iPAccountDetection.model.repository;

import java.util.List;
import java.util.Map;

public interface ILimitedAccountRepository {
    void addLimitedAccount(String accountId, String accountName);
    void removeLimitedAccount(String accountId);
    Map<String, String> getLimitedAccounts();
    void clearLimitedAccounts();
    boolean isLimitedAccount(String accountId);
}
