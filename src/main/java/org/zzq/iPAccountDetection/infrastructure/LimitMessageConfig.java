package org.zzq.iPAccountDetection.infrastructure;

import java.util.List;

public class LimitMessageConfig {
    private final boolean enable;
    private final List<String> messages;

    public LimitMessageConfig(boolean enable, List<String> messages) {
        this.enable = enable;
        this.messages = messages;
    }

    // Getter方法
    public boolean isEnable() {
        return enable;
    }
    public List<String> getMessages() {
        return messages;
    }
}

