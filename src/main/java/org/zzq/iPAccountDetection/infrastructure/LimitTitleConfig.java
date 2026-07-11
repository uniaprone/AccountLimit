package org.zzq.iPAccountDetection.infrastructure;

public class LimitTitleConfig {
    private final boolean enable;
    private final int fadeInTicks;
    private final int stayTicks;
    private final int fadeOutTicks;
    private final String titleMessage;
    private final String titleColor;
    private final String subTitleMessage;
    private final String subTitleColor;

    public LimitTitleConfig(boolean enable,
                            int fadeInTicks, int stayTicks, int fadeOutTicks,
                            String titleMessage, String titleColor,
                            String subTitleMessage, String subTitleColor) {
        this.enable = enable;
        this.fadeInTicks = fadeInTicks;
        this.stayTicks = stayTicks;
        this.fadeOutTicks = fadeOutTicks;
        this.titleMessage = titleMessage;
        this.titleColor = titleColor;
        this.subTitleMessage = subTitleMessage;
        this.subTitleColor = subTitleColor;
    }

    // Getter方法
    public boolean isEnable() {
        return enable;
    }
    public int getFadeInTicks() {
        return fadeInTicks;
    }
    public int getStayTicks() {
        return stayTicks;
    }
    public int getFadeOutTicks() {
        return fadeOutTicks;
    }
    public String getTitleMessage() {
        return titleMessage;
    }
    public String getTitleColor() {
        return titleColor;
    }
    public String getSubTitleMessage() {
        return subTitleMessage;
    }
    public String getSubTitleColor() {
        return subTitleColor;
    }
}

