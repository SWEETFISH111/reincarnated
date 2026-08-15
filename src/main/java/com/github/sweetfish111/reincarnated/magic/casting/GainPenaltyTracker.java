package com.github.sweetfish111.reincarnated.magic.casting;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;

/**
 * 同一グループの出力ノード（例：ADD_MASO）が同一詠唱内で複数回発火したときの
 * 収穫逓減を適用するユーティリティ。
 *
 *   totalGain = Σ(各ノードの生成量) ÷ n^0.2
 *
 * 呼び出しごとに「これまでの生の合計」を更新し、「調整後の目標合計」との差分だけを
 * 実際の付与量として返す。これにより実行順序に関わらず、最終的な合計が
 * 上式に自然に収束する（1個目だけ得、後続だけ損、という不公平が起きない）。
 */
public class GainPenaltyTracker {
    private static final double PENALTY_EXPONENT = 0.2;

    public static float applyAndGetDelta(MagicContext context, String group, float rawAmount) {
        String sumKey = "gainPenalty:" + group + ":rawSum";
        String countKey = "gainPenalty:" + group + ":count";
        String grantedKey = "gainPenalty:" + group + ":granted";

        double rawSum = getDouble(context, sumKey) + rawAmount;
        int count = getInt(context, countKey) + 1;

        double adjustedTotal = rawSum / Math.pow(count, PENALTY_EXPONENT);
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