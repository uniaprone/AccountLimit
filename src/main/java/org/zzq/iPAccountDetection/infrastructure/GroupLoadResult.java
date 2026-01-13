package org.zzq.iPAccountDetection.infrastructure;

import org.zzq.iPAccountDetection.model.aggregate.Group;

import java.util.Map;

// 定义返回数据的容器类
public class GroupLoadResult {
    private final Map<String, Group> groupMap;
    private final Map<String, String> accountToGroupMap;
    private final Map<String, String> ipToGroupMap;

    public GroupLoadResult(Map<String, Group> groupMap,
                           Map<String, String> accountToGroupMap,
                           Map<String, String> ipToGroupMap) {
        this.groupMap = groupMap;
        this.accountToGroupMap = accountToGroupMap;
        this.ipToGroupMap = ipToGroupMap;
    }

    // Getter方法
    public Map<String, Group> getGroupMap() { return groupMap; }
    public Map<String, String> getAccountToGroupMap() { return accountToGroupMap; }
    public Map<String, String> getIpToGroupMap() { return ipToGroupMap; }
}
