package org.zzq.iPAccountDetection.model.entity;

public class Account {
    private String groupId;
    private String accountId;
    private String accountName;
    private boolean isMain;

    public Account(String groupId, String accountId, String accountName, boolean isMain) {
        this.groupId = groupId;
        this.accountId = accountId;
        this.accountName = accountName;
        this.isMain = isMain;
    }

    public Account() {}

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public boolean isMain() {
        return isMain;
    }

    public void setMain(boolean main) {
        isMain = main;
    }
}
