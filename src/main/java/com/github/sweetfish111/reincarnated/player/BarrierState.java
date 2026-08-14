package com.github.sweetfish111.reincarnated.player;

import net.minecraft.nbt.CompoundTag;

public class BarrierState implements PersistentComponent {
    private float currentPoint = 0;
    private float baseMaxBarrierPoint = 20; // 進化などで解放される"床"の容量

    private double totalDamageEncountered = 0.0; // 防御成長量 G_def の元になる被弾量の累積
    private double heavyHitScore = 0.0;          // 大ダメージ判定の蓄積
    private double chipHitScore = 0.0;           // 小ダメージ連続被弾の蓄積
    private double barrierAdaptR = 0.5;          // 0=完全チップ型(減衰率へ伸びる)、1=完全ヘビー型(容量へ伸びる)
    private long lastBarrierHitTick = -1;

    private static final double BARRIER_EPSILON = 0.001;
    private static final double BARRIER_ADAPT_ALPHA = 0.05;
    private static final double BARRIER_GROWTH_DIVISOR = 200.0;
    private static final double BARRIER_CAPACITY_K = 5.0;
    private static final double BARRIER_REDUCTION_K = 0.15;
    private static final float BARRIER_HEAVY_HIT_THRESHOLD_RATIO = 0.5f;
    private static final int BARRIER_CHIP_INTERVAL_TICKS = 60;
    private static final float BASE_BARRIER_DAMAGE_REDUCTION = 0.35f;
    private static final float MAX_BARRIER_DAMAGE_REDUCTION = 0.9f;

    // ★追加：バリア残量そのもの（旧 getBarrierPoint()/setBarrierPoint() に対応）
    public float getCurrentPoint(){ return currentPoint; }
    public void setCurrentPoint(float point){ this.currentPoint = point; }

    public float getMaxBarrierPoint(){
        double capacityBonus = getDefenseGrowth() * barrierAdaptR * BARRIER_CAPACITY_K;
        return (float) (baseMaxBarrierPoint + capacityBonus);
    }

    public float getBaseMaxBarrierPoint(){ return baseMaxBarrierPoint; }
    public void setMaxBarrierPoint(float max){ this.baseMaxBarrierPoint = max; }

    private double getDefenseGrowth(){
        double scaled = totalDamageEncountered / BARRIER_GROWTH_DIVISOR;
        return Math.log(1.0 + scaled);
    }

    public float getBarrierDamageReduction(){
        double reductionBonus = getDefenseGrowth() * (1.0 - barrierAdaptR) * BARRIER_REDUCTION_K;
        float reduction = (float) (BASE_BARRIER_DAMAGE_REDUCTION + reductionBonus);
        return Math.min(reduction, MAX_BARRIER_DAMAGE_REDUCTION);
    }

    public void recordBarrierHit(float rawDamage, boolean barrierBroke, long currentTick){
        totalDamageEncountered += rawDamage;

        boolean isHeavy = barrierBroke
                || (baseMaxBarrierPoint > 0 && rawDamage >= baseMaxBarrierPoint * BARRIER_HEAVY_HIT_THRESHOLD_RATIO);

        if (isHeavy) {
            heavyHitScore += 1.0;
        } else if (lastBarrierHitTick >= 0 && (currentTick - lastBarrierHitTick) < BARRIER_CHIP_INTERVAL_TICKS) {
            chipHitScore += 1.0;
        }
        lastBarrierHitTick = currentTick;

        double rInstant = heavyHitScore / (heavyHitScore + chipHitScore + BARRIER_EPSILON);
        barrierAdaptR = barrierAdaptR * (1.0 - BARRIER_ADAPT_ALPHA) + rInstant * BARRIER_ADAPT_ALPHA;
    }

    @Override
    public void saveToNBT(CompoundTag tag) {
        tag.putFloat("currentBarrier", currentPoint);
        tag.putFloat("maxBarrier", baseMaxBarrierPoint);
        tag.putDouble("totalDamageEncountered", totalDamageEncountered);
        tag.putDouble("heavyHitScore", heavyHitScore);
        tag.putDouble("chipHitScore", chipHitScore);
        tag.putDouble("barrierAdaptR", barrierAdaptR);
        tag.putDouble("lastBarrierHitTick", (double) lastBarrierHitTick);
    }

    @Override
    public void loadFromNBT(CompoundTag tag) {
        currentPoint = tag.getFloatOr("currentBarrier", 0.0f);
        baseMaxBarrierPoint = tag.getFloatOr("maxBarrier", 20.0f);
        totalDamageEncountered = tag.getDouble("totalDamageEncountered").orElse(0.0);
        heavyHitScore = tag.getDouble("heavyHitScore").orElse(0.0);
        chipHitScore = tag.getDouble("chipHitScore").orElse(0.0);
        barrierAdaptR = tag.getDouble("barrierAdaptR").orElse(0.5);
        lastBarrierHitTick = (long)(double) tag.getDouble("lastBarrierHitTick").orElse(-1.0);
    }
}