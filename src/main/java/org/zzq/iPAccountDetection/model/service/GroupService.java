package org.zzq.iPAccountDetection.model.service;

import org.zzq.iPAccountDetection.infrastructure.LogUtil;
import org.zzq.iPAccountDetection.model.entity.Account;
import org.zzq.iPAccountDetection.model.aggregate.Group;
import org.zzq.iPAccountDetection.model.entity.IP;

import java.util.List;
import java.util.UUID;

public class GroupService {
    private LogUtil logger;

    public GroupService(LogUtil logger) {
        this.logger = logger;
    }


}
