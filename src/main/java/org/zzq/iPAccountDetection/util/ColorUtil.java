package org.zzq.iPAccountDetection.util;

import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.Nullable;

public class ColorUtil {
    @Nullable
    public static NamedTextColor convertColor(String color) {
        if (color == null) {
            return null;
        }

        // 转换为小写并去除下划线/空格，提高容错性
        String normalizedColor = color.toLowerCase().replace("_", "").replace(" ", "");

        switch (normalizedColor) {
            case "black":
                return NamedTextColor.BLACK;
            case "darkblue":
                return NamedTextColor.DARK_BLUE;
            case "darkgreen":
                return NamedTextColor.DARK_GREEN;
            case "darkaqua":
                return NamedTextColor.DARK_AQUA;
            case "darkred":
                return NamedTextColor.DARK_RED;
            case "darkpurple":
                return NamedTextColor.DARK_PURPLE;
            case "gold":
                return NamedTextColor.GOLD;
            case "gray":
            case "grey":
                return NamedTextColor.GRAY;
            case "darkgray":
            case "darkgrey":
                return NamedTextColor.DARK_GRAY;
            case "blue":
                return NamedTextColor.BLUE;
            case "green":
                return NamedTextColor.GREEN;
            case "aqua":
                return NamedTextColor.AQUA;
            case "red":
                return NamedTextColor.RED;
            case "lightpurple":
                return NamedTextColor.LIGHT_PURPLE;
            case "yellow":
                return NamedTextColor.YELLOW;
            case "white":
                return NamedTextColor.WHITE;
            default:
                return NamedTextColor.WHITE; // 或者返回一个默认颜色，如 NamedTextColor.WHITE
        }
    }
}
