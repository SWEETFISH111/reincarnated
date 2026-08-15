package com.github.sweetfish111.reincarnated.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class BalanceConfig {
    public static final ModConfigSpec SPEC;

    // ===== 魔素経済（MasoEconomy） =====
    public static final ModConfigSpec.DoubleValue STAGE_CARRYOVER_RATIO;
    public static final ModConfigSpec.DoubleValue REGEN_RECOVERY_MIDPOINT_RATIO;
    public static final ModConfigSpec.DoubleValue MASO_SCALE_DIVISOR;
    public static final ModConfigSpec.DoubleValue STYLE_ALPHA;
    public static final ModConfigSpec.DoubleValue STYLE_GROWTH_DIVISOR;
    public static final ModConfigSpec.DoubleValue STYLE_REFERENCE_T_TICKS;
    public static final ModConfigSpec.DoubleValue STYLE_K_REGEN;
    public static final ModConfigSpec.DoubleValue BURST_RATIO_THRESHOLD;
    public static final ModConfigSpec.IntValue SUSTAIN_INTERVAL_TICKS;

    // ===== バリア（BarrierState） =====
    public static final ModConfigSpec.DoubleValue BARRIER_ADAPT_ALPHA;
    public static final ModConfigSpec.DoubleValue BARRIER_GROWTH_DIVISOR;
    public static final ModConfigSpec.DoubleValue BARRIER_CAPACITY_K;
    public static final ModConfigSpec.DoubleValue BARRIER_REDUCTION_K;
    public static final ModConfigSpec.DoubleValue BARRIER_HEAVY_HIT_THRESHOLD_RATIO;
    public static final ModConfigSpec.IntValue BARRIER_CHIP_INTERVAL_TICKS;
    public static final ModConfigSpec.DoubleValue BASE_BARRIER_DAMAGE_REDUCTION;
    public static final ModConfigSpec.DoubleValue MAX_BARRIER_DAMAGE_REDUCTION;

    // ===== 魔素投資カーブ（MasoInvestmentScaling） =====
    public static final ModConfigSpec.DoubleValue SAFE_COST_EXPONENT;
    public static final ModConfigSpec.DoubleValue OVERCHARGE_COST_EXPONENT;
    public static final ModConfigSpec.DoubleValue OVERCHARGE_THRESHOLD_RATIO;

    // ===== 詠唱時間コスト（CastCostCalculator） =====
    public static final ModConfigSpec.DoubleValue BASE_CAST_TICKS;
    public static final ModConfigSpec.DoubleValue CAST_DEPTH_COEFFICIENT;
    public static final ModConfigSpec.DoubleValue CAST_DEPTH_EXPONENT;
    public static final ModConfigSpec.DoubleValue CAST_WIDTH_COEFFICIENT;
    public static final ModConfigSpec.DoubleValue CAST_WIDTH_EXPONENT;

    // ===== 複製ペナルティ（GainPenaltyTracker） =====
    public static final ModConfigSpec.DoubleValue GAIN_PENALTY_EXPONENT;

    // ===== 常駐実行（ActiveMagicManager） =====
    public static final ModConfigSpec.IntValue RESIDENT_NODE_INTERVAL_TICKS;

    // ===== 召喚（SummonManager） =====
    public static final ModConfigSpec.IntValue MAX_SUMMONS_PER_OWNER;

    // ===== ユニークスキル進化 =====
    public static final ModConfigSpec.DoubleValue UNIQUE_SKILL_EVOLUTION_THRESHOLD;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("maso_economy");
        STAGE_CARRYOVER_RATIO = builder
                .comment("ステージ進化時、旧ステージの超過消費分をどれだけ新ステージへ持ち越すか(0-1)")
                .defineInRange("stageCarryoverRatio", 0.3, 0.0, 1.0);
        REGEN_RECOVERY_MIDPOINT_RATIO = builder
                .comment("ステージ進化時、回復速度が新ステージの床値からどれだけ引き継がれるか(0-1)")
                .defineInRange("regenRecoveryMidpointRatio", 0.5, 0.0, 1.0);
        MASO_SCALE_DIVISOR = builder
                .comment("魔素上限・回復速度の対数スケーリングにおける割数")
                .defineInRange("masoScaleDivisor", 250.0, 1.0, 10000.0);
        STYLE_ALPHA = builder
                .comment("バースト/継続スタイル推定のEMA追従速度")
                .defineInRange("styleAlpha", 0.02, 0.0001, 1.0);
        STYLE_GROWTH_DIVISOR = builder
                .comment("スタイルボーナスの対数成長における割数")
                .defineInRange("styleGrowthDivisor", 250.0, 1.0, 10000.0);
        STYLE_REFERENCE_T_TICKS = builder
                .comment("バースト/継続の等価性を保証する基準時間(tick)")
                .defineInRange("styleReferenceTTicks", 400.0, 20.0, 12000.0);
        STYLE_K_REGEN = builder
                .comment("スタイルボーナスの回復側係数(k_max = これ×基準時間で自動校正)")
                .defineInRange("styleKRegen", 0.02, 0.0, 10.0);
        BURST_RATIO_THRESHOLD = builder
                .comment("現在maxMasoの何割消費でバースト判定とするか")
                .defineInRange("burstRatioThreshold", 0.5, 0.0, 1.0);
        SUSTAIN_INTERVAL_TICKS = builder
                .comment("この間隔(tick)以内の連続詠唱を継続型として加算する")
                .defineInRange("sustainIntervalTicks", 100, 1, 6000);
        builder.pop();

        builder.push("barrier");
        BARRIER_ADAPT_ALPHA = builder
                .comment("heavy/chip適応のEMA追従速度")
                .defineInRange("barrierAdaptAlpha", 0.05, 0.0001, 1.0);
        BARRIER_GROWTH_DIVISOR = builder
                .comment("防御成長量の対数スケーリングにおける割数")
                .defineInRange("barrierGrowthDivisor", 200.0, 1.0, 10000.0);
        BARRIER_CAPACITY_K = builder
                .comment("heavy適応による容量ボーナスの伸び幅")
                .defineInRange("barrierCapacityK", 5.0, 0.0, 100.0);
        BARRIER_REDUCTION_K = builder
                .comment("chip適応による減衰率ボーナスの伸び幅")
                .defineInRange("barrierReductionK", 0.15, 0.0, 10.0);
        BARRIER_HEAVY_HIT_THRESHOLD_RATIO = builder
                .comment("床容量の何割消費で大ダメージ(heavy)扱いとするか")
                .defineInRange("barrierHeavyHitThresholdRatio", 0.5, 0.0, 1.0);
        BARRIER_CHIP_INTERVAL_TICKS = builder
                .comment("この間隔(tick)以内の連続被弾をchip扱いとする")
                .defineInRange("barrierChipIntervalTicks", 60, 1, 6000);
        BASE_BARRIER_DAMAGE_REDUCTION = builder
                .comment("バリア展開中の基礎ダメージ半減率")
                .defineInRange("baseBarrierDamageReduction", 0.35, 0.0, 0.99);
        MAX_BARRIER_DAMAGE_REDUCTION = builder
                .comment("半減率の上限(ほぼ無敵化を防ぐ)")
                .defineInRange("maxBarrierDamageReduction", 0.9, 0.0, 0.99);
        builder.pop();

        builder.push("maso_investment_scaling");
        SAFE_COST_EXPONENT = builder
                .comment("安全域のコスト凸カーブ指数(大きいほど大量要求が急激に割高になる)")
                .defineInRange("safeCostExponent", 1.3, 1.0, 5.0);
        OVERCHARGE_COST_EXPONENT = builder
                .comment("オーバーチャージ域のコスト凸カーブ指数")
                .defineInRange("overchargeCostExponent", 2.2, 1.0, 6.0);
        OVERCHARGE_THRESHOLD_RATIO = builder
                .comment("現在魔素量に対して、コストがこの比率を超えるとオーバーチャージ域に切り替わる")
                .defineInRange("overchargeThresholdRatio", 0.5, 0.01, 1.0);
        builder.pop();

        builder.push("cast_cost");
        BASE_CAST_TICKS = builder
                .comment("最低詠唱時間(tick)")
                .defineInRange("baseCastTicks", 5.0, 0.0, 200.0);
        CAST_DEPTH_COEFFICIENT = builder
                .comment("クリティカルパス深さコストの係数k_t")
                .defineInRange("castDepthCoefficient", 2.0, 0.0, 20.0);
        CAST_DEPTH_EXPONENT = builder
                .comment("クリティカルパス深さコストの指数")
                .defineInRange("castDepthExponent", 1.3, 1.0, 3.0);
        CAST_WIDTH_COEFFICIENT = builder
                .comment("総ノード数コストの係数k_w")
                .defineInRange("castWidthCoefficient", 0.5, 0.0, 20.0);
        CAST_WIDTH_EXPONENT = builder
                .comment("総ノード数コストの指数")
                .defineInRange("castWidthExponent", 0.8, 0.1, 3.0);
        builder.pop();

        builder.push("gain_penalty");
        GAIN_PENALTY_EXPONENT = builder
                .comment("同一グループの複製ノードによる収穫逓減の指数(n^この値で割る)")
                .defineInRange("gainPenaltyExponent", 0.2, 0.0, 1.0);
        builder.pop();

        builder.push("active_magic");
        RESIDENT_NODE_INTERVAL_TICKS = builder
                .comment("常駐ノード(ON_TICK)のデフォルト実行間隔(tick)")
                .defineInRange("residentNodeIntervalTicks", 20, 1, 1200);
        builder.pop();

        builder.push("summon");
        MAX_SUMMONS_PER_OWNER = builder
                .comment("プレイヤー1人あたりの同時召喚上限数")
                .defineInRange("maxSummonsPerOwner", 3, 1, 20);
        builder.pop();

        builder.push("unique_skill");
        UNIQUE_SKILL_EVOLUTION_THRESHOLD = builder
                .comment("ユニークスキルの各スコアが進化条件を満たす閾値")
                .defineInRange("uniqueSkillEvolutionThreshold", 100.0, 1.0, 100000.0);
        builder.pop();

        SPEC = builder.build();
    }
}