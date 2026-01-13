package org.zzq.iPAccountDetection.model.entity;

public class IP {
    private String groupId;
    private String ip;
    private long lastLogin;

    public IP(String groupId, String ip, long last_login) {
        this.groupId = groupId;
        this.ip = ip;
        this.lastLogin = last_login;
    }

    public IP(){}

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public boolean isExpireIP(long currentTime, long expireTime){
        if(currentTime - lastLogin >= expireTime){
            return true;
        }
        return false;
    }
}
