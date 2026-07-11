package org.zzq.iPAccountDetection.infrastructure;

public class LimitEventConfig {
    private boolean isEnable;
    private final int triggerIntervalTimes;
    private final LimitTitleConfig titleConfig;
    private final LimitMessageConfig messageConfig;

    public LimitEventConfig(boolean isEnable,
                            int triggerIntervalTimes,
                            LimitTitleConfig titleConfig,
                            LimitMessageConfig messageConfig) {
        this.isEnable = isEnable;
        this.triggerIntervalTimes = triggerIntervalTimes;
        this.titleConfig = titleConfig;
        this.messageConfig = messageConfig;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }

    // Getters
    public int getTriggerIntervalTimes() {
        return triggerIntervalTimes;
    }
    public LimitTitleConfig getTitleConfig() {
        return titleConfig;
    }
    public LimitMessageConfig getMessageConfig() {
        return messageConfig;
    }
}