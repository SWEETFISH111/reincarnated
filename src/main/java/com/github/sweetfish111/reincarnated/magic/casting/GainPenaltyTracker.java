package com.github.sweetfish111.reincarnated.magic.casting;

import com.github.sweetfish111.reincarnated.config.BalanceConfig;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;

public class GainPenaltyTracker {

    public static float applyAndGetDelta(MagicContext context, String group, float rawAmount) {
        String sumKey = "gainPenalty:" + group + ":rawSum";
        String countKey = "gainPenalty:" + group + ":count";
        String grantedKey = "gainPenalty:" + group + ":granted";

        double rawSum = getDouble(context, sumKey) + rawAmount;
        int count = getInt(context, countKey) + 1;

        double adjustedTotal = rawSum / Math.pow(count, BalanceConfig.GAIN_PENALTY_EXPONENT.get());
        double grantedSoFar = getDouble(context, grantedKey);
        double delta = adjustedTotal - grantedSoFar;

        context.setMagicValue(sumKey, rawSum);
        context.setMagicValue(countKey, count);
        context.setMagicValue(grantedKey, adjustedTotal);

        return (float) delta;
    }

    private static double getDouble(MagicContext context, String key) {
        Object val = context.getMagicValue(key);
        return (val instanceof Number n) ? n.doubleValue() : 0.0;
    }

    private static int getInt(MagicContext context, String key) {
        Object val = context.getMagicValue(key);
        return (val instanceof Number n) ? n.intValue() : 0;
    }
}