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
     * computeEffectの戻り値。
     * effectAmount: 投入魔素量から算出された効果量
     * masoCost    : 実際に消費される魔素量（＝investedMasoそのもの。呼び出し側の記述を統一するために保持）
     * isOvercharge: 過剰域（オーバーロードの崖の向こう側）に突入したか
     */
    public record EffectResult(float effectAmount, float masoCost, boolean isOvercharge) {
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

        // ★computeEffectでは extra に (1+ボーナス率) を掛けたものが最終効果量に含まれているため、
        //   逆算時はまずボーナス分を割り戻してから「超過分(extra)」を復元する必要がある。
        //   ここを割り戻さずに (desiredAmount - desiredAtThreshold) をそのまま冪乗すると、
        //   ボーナスで水増しされた分まで含めて必要投入量を計算してしまい、
        //   computeEffectとの往復（投入→効果→逆算）で必要投入量が実際より多く出てしまう。
        double bonusRatio = BalanceConfig.OVERLOAD_EFFECT_BONUS_RATIO.get();
        double extraAmount = Math.max(0, (desiredAmount - desiredAtThreshold) / (1.0 + bonusRatio));
        double overloadCost = baseCostPerUnit * Math.pow(extraAmount, BalanceConfig.OVERLOAD_COST_EXPONENT.get());

        // ★過剰域：要求量に加えて、超過分に比例したボーナス効果を上乗せする（リスクに見合うリターン）
        double bonusAmount = extraAmount * bonusRatio;
        float grantedAmount = (float) (desiredAmount + bonusAmount);

        return new CostResult((float) (threshold + overloadCost), grantedAmount, true);
    }

    /**
     * computeCostの厳密な逆関数。
     * 「望む効果量→コスト」ではなく「投入した魔素量→効果量」を算出する。
     * 投入量が安全域の閾値(availableMaso×閾値比率)以下なら安全域カーブの逆算、
     * 閾値を超えた分は過剰域カーブの逆算＋オーバーロードボーナスを上乗せする。
     * <p>
     * 消費される魔素量は投入量そのもの（investedMaso）。
     * 実際に足りるかどうか（MasoShortageExceptionを投げるか）は
     * 呼び出し側のconsumeMaso()に委ねる。
     */
    public static EffectResult computeEffect(float baseCostPerUnit, float investedMaso, float availableMaso) {
        if (investedMaso <= 0 || baseCostPerUnit <= 0) return new EffectResult(0f, Math.max(0f, investedMaso), false);

        double safeExponent = BalanceConfig.SAFE_COST_EXPONENT.get();
        float threshold = availableMaso * BalanceConfig.OVERLOAD_THRESHOLD_RATIO.get().floatValue();

        if (threshold <= 0 || investedMaso <= threshold) {
            // 安全域：safeCostの逆関数（＝maxAffordableSafeAmountと同じ形）をBalanceConfigの指数で計算
            double effectAmount = Math.pow(investedMaso / baseCostPerUnit, 1.0 / safeExponent);
            return new EffectResult((float) effectAmount, investedMaso, false);
        }

        // 過剰域：閾値までの効果量 ＋ 超過投資分から逆算した追加効果量 ＋ オーバーロードボーナス
        double desiredAtThreshold = Math.pow(threshold / baseCostPerUnit, 1.0 / safeExponent);

        double overloadExponent = BalanceConfig.OVERLOAD_COST_EXPONENT.get();
        double extraCost = investedMaso - threshold;
        double extraAmount = Math.pow(extraCost / baseCostPerUnit, 1.0 / overloadExponent);

        double bonusAmount = extraAmount * BalanceConfig.OVERLOAD_EFFECT_BONUS_RATIO.get();
        float effectAmount = (float) (desiredAtThreshold + extraAmount + bonusAmount);

        return new EffectResult(effectAmount, investedMaso, true);
    }
}