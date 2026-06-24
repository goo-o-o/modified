package com.goo.modified.utils;

import net.minecraft.util.FastColor;

import java.util.List;

public class ColorUtils {
    public static int getGradientAt(float x, float spread, float speed, List<Integer> colors) {
        // Use the game's actual clock for smooth animation that respects pause menus
        // Divide by 1000 to convert millis to seconds
        float time = (System.currentTimeMillis() % 100000L) / 1000.0F;

        // Offset is (position / spread) + (time * speed)
        // We multiply speed by time so the wave moves
        float progress = ((x / spread) + (time * speed)) % 1.0F;
        if (progress < 0) progress += 1.0F;

        int numColors = colors.size();
        float scaledProgress = progress * numColors;
        int index = (int) scaledProgress;
        int nextIndex = (index + 1) % numColors;
        float segmentProgress = scaledProgress - index;

        return FastColor.ARGB32.lerp(segmentProgress, colors.get(index), colors.get(nextIndex));
    }
}
