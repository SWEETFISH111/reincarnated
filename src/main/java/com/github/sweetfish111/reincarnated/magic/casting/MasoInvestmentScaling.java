package com.github.sweetfish111.reincarnated.magic.casting;

import com.github.sweetfish111.reincarnated.config.BalanceConfig;

/**
 * 「望む効果量」に対する魔素コストを算出する共通ユーティリティ。
 * 安全域は緩やかな凸カーブ（大量要求ほど単位コストが上がる＝分散投資が得）、
 * 利用可能魔素に対する比率が閾値を超えると、より急なオーバーチャージ域に切り替わる「崖」構造。
 */
public class MasoInvestmentScaling {
    private static final double SAFE_COST_EXPONENT = 1.3;
    private static final double OVERCHARGE_COST_EXPONENT = 2.2;
    private static final double OVERCHARGE_THRESHOLD_RATIO = 0.5;

    public record CostResult(float cost, float grantedAmount, boolean isOvercharge) {
    }

    /**
     * 安全域のみのコスト（オーバーロードを起こさせたくないノード向け）
     */
    public static float safeCost(float baseCostPerUnit, float desiredAmount) {
        if (desiredAmount <= 0) return 0f;
        return (float) (baseCostPerUnit * Math.pow(desiredAmount, SAFE_COST_EXPONENT));
    }

    /**
     * safeCostの逆関数：予算内で賄える最大の効果量
     */
    public static float maxAffordableSafeAmount(float baseCostPerUnit, float availableMaso) {
        if (availableMaso <= 0 || baseCostPerUnit <= 0) return 0f;
        return (float) Math.pow(availableMaso / baseCostPerUnit, 1.0 / SAFE_COST_EXPONENT);
    }

    /**
     * オーバーロードの崖を含むフルカーブ。
     * 安全域のコストがavailableMaso×閾値比率を超えると、超過分だけ急な凸カーブに切り替わる。
     */
    public static CostResult computeCost(float baseCostPerUnit, float desiredAmount, float availableMaso) {
        if (desiredAmount <= 0) return new CostResult(0f, 0f, false);

        float safeCost = safeCost(baseCostPerUnit, desiredAmount);
        float threshold = availableMaso * BalanceConfig.OVERLOAD_THRESHOLD_RATIO.get().floatValue();

        if (threshold <= 0 || safeCost <= threshold) {
            return new CostResult(safeCost, desiredAmount, false); // 安全域は要求量＝付与量のまま
        }

        double safeExponent = BalanceConfig.SAFE_COST_EXPONENT.get();
        double desiredAtThreshold = Math.pow(threshold / baseCostPerUnit, 1.0 / safeExponent);
        double extraAmount = Math.max(0, desiredAmount - desiredAtThreshold);
        double overloadCost = baseCostPerUnit * Math.pow(extraAmount, BalanceConfig.OVERLOAD_COST_EXPONENT.get());

        // ★過剰域：要求量に加えて、超過分に比例したボーナス効果を上乗せする（リスクに見合うリターン）
        double bonusAmount = extraAmount * BalanceConfig.OVERLOAD_EFFECT_BONUS_RATIO.get();
        float grantedAmount = (float) (desiredAmount + bonusAmount);

        return new CostResult((float) (threshold + overloadCost), grantedAmount, true);
    }
}