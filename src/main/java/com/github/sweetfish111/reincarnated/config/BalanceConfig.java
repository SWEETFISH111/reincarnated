package com.github.sweetfish111.reincarnated.config;

import com.github.sweetfish111.reincarnated.world.LandMasoDensityData;
import net.neoforged.neoforge.common.ModConfigSpec;

public class BalanceConfig {
    public static final ModConfigSpec SPEC;

    //==== 魔法ダメージ ====
    public static final ModConfigSpec.IntValue DAMAGE_NODE_COOLDOWN_TICKS;

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
    public static final ModConfigSpec.DoubleValue OVERLOAD_COST_EXPONENT;
    public static final ModConfigSpec.DoubleValue OVERLOAD_THRESHOLD_RATIO;
    public static final ModConfigSpec.DoubleValue OVERLOAD_EFFECT_BONUS_RATIO;

    // ===== アクションノード基礎コスト（各ノードのBASECOST。get_base_costノードもここを参照する） =====
    public static final ModConfigSpec.DoubleValue DAMAGE_BASE_COST;
    public static final ModConfigSpec.DoubleValue HEALING_BASE_COST;
    public static final ModConfigSpec.DoubleValue EXPLOSION_BASE_COST;
    public static final ModConfigSpec.DoubleValue DIG_BASE_COST;
    public static final ModConfigSpec.DoubleValue COLLECT_ITEMS_BASE_COST;
    public static final ModConfigSpec.DoubleValue SUMMON_BASE_COST;

    //==== 演算能力系 ====
    public static final ModConfigSpec.DoubleValue BASE_COMPUTE_CAPACITY;
    public static final ModConfigSpec.DoubleValue COMPUTE_CAPACITY_SCALE;
    public static final ModConfigSpec.DoubleValue COMPUTE_CAPACITY_STAGE_EXPONENT;
    public static final ModConfigSpec.DoubleValue COMPUTE_CAPACITY_DIVISOR;
    public static final ModConfigSpec.DoubleValue COMPUTE_CAPACITY_MIN_CAST_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue COMPUTE_CAPACITY_CAST_HALF_LIFE;

    //===== 魔素タンク容量 ====
    public static final ModConfigSpec.DoubleValue TANK_BASE_CAPACITY;
    public static final ModConfigSpec.DoubleValue TANK_CAPACITY_PER_COMPUTE;

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

    //==== 土地魔素濃度関連 ====
    public static final ModConfigSpec.DoubleValue MASO_DENSITY_NORMAL_BASE;
    public static final ModConfigSpec.DoubleValue MASO_DENSITY_LOW_BASE;
    public static final ModConfigSpec.DoubleValue MASO_DENSITY_VERY_LOW_BASE;
    public static final ModConfigSpec.DoubleValue MASO_DENSITY_HIGH_BASE;
    public static final ModConfigSpec.DoubleValue MASO_DENSITY_VERY_HIGH_BASE;
    public static final ModConfigSpec.DoubleValue MASO_DENSITY_NOISE_AMPLITUDE;
    public static final ModConfigSpec.DoubleValue MASO_DENSITY_NOISE_SCALE;
    public static final ModConfigSpec.DoubleValue MASO_DENSITY_DAMPING_DISTANCE;

    public static final ModConfigSpec.DoubleValue MASO_DENSITY_RING_DISTANCE;
    public static final ModConfigSpec.DoubleValue MASO_DENSITY_RING_INCREMENT;
    public static final ModConfigSpec.DoubleValue MASO_MOB_POWER_MIN_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue MASO_MOB_POWER_MAX_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue MASO_STONE_BASE_DROP_CHANCE;
    public static final ModConfigSpec.DoubleValue MASO_STONE_DROP_SCALE;
    public static final ModConfigSpec.DoubleValue MASO_STONE_MAX_DROP_CHANCE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("magic_damage");
        DAMAGE_NODE_COOLDOWN_TICKS = builder
                .comment("DamageNode単体が同一tick内で連続ダメージを与えられるまでの最小間隔(tick)")
                .defineInRange("damageNodeCooldownTicks", 4, 0, 200);
        builder.pop();

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

        OVERLOAD_COST_EXPONENT = builder
                .comment("オーバーロード域のコスト凸カーブ指数")
                .defineInRange("overchargeCostExponent", 2.2, 1.0, 6.0);
        OVERLOAD_THRESHOLD_RATIO = builder
                .comment("現在魔素量に対して、コストがこの比率を超えるとオーバーロード域に切り替わる")
                .defineInRange("overchargeThresholdRatio", 0.5, 0.01, 1.0);
        OVERLOAD_EFFECT_BONUS_RATIO = builder
                .comment("過剰域で超過投資した分に対する効果ボーナス倍率（例：0.4なら超過分の40%が効果に上乗せされる）")
                .defineInRange("overchargeEffectBonusRatio", 0.4, 0.0, 5.0);
        builder.pop();

        builder.push("action_node_base_cost");
        DAMAGE_BASE_COST = builder
                .comment("DamageNodeの基礎コスト係数")
                .defineInRange("damageBaseCost", 2.0, 0.0, 1000.0);
        HEALING_BASE_COST = builder
                .comment("HealingNodeの基礎コスト係数")
                .defineInRange("healingBaseCost", 5.0, 0.0, 1000.0);
        EXPLOSION_BASE_COST = builder
                .comment("ExplosionNodeの基礎コスト係数")
                .defineInRange("explosionBaseCost", 4.0, 0.0, 1000.0);
        DIG_BASE_COST = builder
                .comment("DigNodeの基礎コスト係数")
                .defineInRange("digBaseCost", 0.7, 0.0, 1000.0);
        COLLECT_ITEMS_BASE_COST = builder
                .comment("CollectItemsNodeの基礎コスト係数")
                .defineInRange("collectItemsBaseCost", 0.2, 0.0, 1000.0);
        SUMMON_BASE_COST = builder
                .comment("SummonNodeの基礎コスト係数")
                .defineInRange("summonBaseCost", 1.2, 0.0, 1000.0);
        builder.pop();

        builder.push("compute_capacity");
        BASE_COMPUTE_CAPACITY = builder
                .comment("演算能力の基礎値（詠唱経験ゼロ・STAGE0時点の値）")
                .defineInRange("baseComputeCapacity", 10.0, 0.0, 10000.0);
        COMPUTE_CAPACITY_SCALE = builder
                .comment("累計詠唱時間による対数成長の係数")
                .defineInRange("computeCapacityScale", 8.0, 0.0, 1000.0);
        COMPUTE_CAPACITY_STAGE_EXPONENT = builder
                .comment("進化ステージが成長率に与える影響の強さ（(ステージ順+1)^この値を成長係数に掛ける）")
                .defineInRange("computeCapacityStageExponent", 1.5, 0.0, 5.0);
        COMPUTE_CAPACITY_DIVISOR = builder
                .comment("累計詠唱時間(tick)の対数成長における割数")
                .defineInRange("computeCapacityDivisor", 500.0, 1.0, 100000.0);
        COMPUTE_CAPACITY_MIN_CAST_MULTIPLIER = builder
                .comment("演算能力による詠唱時間短縮の下限倍率（0.3なら最短でも元の30%までしか縮まない）")
                .defineInRange("computeCapacityMinCastMultiplier", 0.1, 0.05, 1.0);
        COMPUTE_CAPACITY_CAST_HALF_LIFE = builder
                .comment("詠唱短縮効果が最大短縮の半分に達する演算能力値")
                .defineInRange("computeCapacityCastHalfLife", 50.0, 1.0, 100000.0);
        builder.pop();

        builder.push("maso_tank");
        TANK_BASE_CAPACITY = builder
                .comment("演算能力ゼロの時点でも保証される、タンクの基礎容量")
                .defineInRange("tankBaseCapacity", 20.0, 0.0, 1000.0);
        TANK_CAPACITY_PER_COMPUTE = builder
                .comment("演算能力1につき、タンク容量がどれだけ増えるか")
                .defineInRange("tankCapacityPerCompute", 1.0, 0.0, 100.0);
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

        builder.push("land_maso_density");
        MASO_DENSITY_NORMAL_BASE = builder
                .comment("タグ未指定バイオームの基礎魔素濃度")
                .defineInRange("masoDensityNormalBase", 10.0, 0.0, 1000.0);
        MASO_DENSITY_LOW_BASE = builder
                .comment("maso_density_lowタグの基礎魔素濃度")
                .defineInRange("masoDensityLowBase", 4.0, 0.0, 1000.0);
        MASO_DENSITY_VERY_LOW_BASE = builder
                .comment("maso_density_very_lowタグの基礎魔素濃度")
                .defineInRange("masoDensityVeryLowBase", 1.0, 0.0, 1000.0);
        MASO_DENSITY_HIGH_BASE = builder
                .comment("maso_density_highタグの基礎魔素濃度")
                .defineInRange("masoDensityHighBase", 25.0, 0.0, 1000.0);
        MASO_DENSITY_VERY_HIGH_BASE = builder
                .comment("maso_density_very_highタグの基礎魔素濃度")
                .defineInRange("masoDensityVeryHighBase", 60.0, 0.0, 1000.0);
        MASO_DENSITY_NOISE_AMPLITUDE = builder
                .comment("地域差ノイズの振幅（基礎値に対して±この値までブレる）")
                .defineInRange("masoDensityNoiseAmplitude", 8.0, 0.0, 500.0);
        MASO_DENSITY_NOISE_SCALE = builder
                .comment("ノイズの空間スケール（チャンク単位。大きいほど広範囲でゆるやかに変化する）")
                .defineInRange("masoDensityNoiseScale", 12.0, 1.0, 500.0);
        MASO_DENSITY_RING_DISTANCE = builder
                .comment("この距離(ブロック)ごとにスポーン地点から1段階濃度が上がる")
                .defineInRange("masoDensityRingDistance", 1000.0, 10.0, 100000.0);
        MASO_DENSITY_RING_INCREMENT = builder
                .comment("リング1段階ごとに加算される濃度")
                .defineInRange("masoDensityRingIncrement", 8.0, 0.0, 1000.0);
        MASO_DENSITY_DAMPING_DISTANCE = builder
                .comment("この距離(ブロック)でノイズ・バイオーム差分の減衰が完全に解ける(フル振幅になる)")
                .defineInRange("masoDensityDampingDistance", 3000.0, 10.0, 100000.0);
        builder.pop();

        builder.push("land_density_mob_scaling");
        MASO_MOB_POWER_MIN_MULTIPLIER = builder
                .comment("濃度によるモブ強化倍率の下限")
                .defineInRange("masoMobPowerMinMultiplier", 0.4, 0.05, 1.0);
        MASO_MOB_POWER_MAX_MULTIPLIER = builder
                .comment("濃度によるモブ強化倍率の上限")
                .defineInRange("masoMobPowerMaxMultiplier", 6.0, 1.0, 50.0);
        MASO_STONE_BASE_DROP_CHANCE = builder
                .comment("倍率1.0(基準地帯)を超えたモブから魔石が落ちる基礎確率")
                .defineInRange("masoStoneBaseDropChance", 0.02, 0.0, 1.0);
        MASO_STONE_DROP_SCALE = builder
                .comment("倍率が基準値を1超えるごとに加算されるドロップ確率")
                .defineInRange("masoStoneDropScale", 0.05, 0.0, 1.0);
        MASO_STONE_MAX_DROP_CHANCE = builder
                .comment("魔石ドロップ確率の上限")
                .defineInRange("masoStoneMaxDropChance", 0.6, 0.0, 1.0);
        builder.pop();

        SPEC = builder.build();
    }
}