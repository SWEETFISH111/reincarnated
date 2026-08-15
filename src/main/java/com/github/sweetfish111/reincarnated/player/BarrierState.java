package com.github.sweetfish111.reincarnated.player;

import com.github.sweetfish111.reincarnated.config.BalanceConfig;
import net.minecraft.nbt.CompoundTag;

public class BarrierState implements PersistentComponent {
    private float currentPoint = 0;
    private float baseMaxBarrierPoint = 20;

    private double totalDamageEncountered = 0.0;
    private double heavyHitScore = 0.0;
    private double chipHitScore = 0.0;
    private double barrierAdaptR = 0.5;
    private long lastBarrierHitTick = -1;

    private static final double BARRIER_EPSILON = 0.001; // ゼロ除算防止のみ、チューニング対象外

    public float getCurrentPoint(){ return currentPoint; }
    public void setCurrentPoint(float point){ this.currentPoint = point; }

    public float getMaxBarrierPoint(){
        double capacityBonus = getDefenseGrowth() * barrierAdaptR * BalanceConfig.BARRIER_CAPACITY_K.get();
        return (float) (baseMaxBarrierPoint + capacityBonus);
    }

    public float getBaseMaxBarrierPoint(){ return baseMaxBarrierPoint; }
    public void setMaxBarrierPoint(float max){ this.baseMaxBarrierPoint = max; }

    private double getDefenseGrowth(){
        double scaled = totalDamageEncountered / BalanceConfig.BARRIER_GROWTH_DIVISOR.get();
        return Math.log(1.0 + scaled);
    }

    public float getBarrierDamageReduction(){
        double reductionBonus = getDefenseGrowth() * (1.0 - barrierAdaptR) * BalanceConfig.BARRIER_REDUCTION_K.get();
        float reduction = (float) (BalanceConfig.BASE_BARRIER_DAMAGE_REDUCTION.get() + reductionBonus);
        return (float) Math.min(reduction, BalanceConfig.MAX_BARRIER_DAMAGE_REDUCTION.get());
    }

    public double getAdaptR(){ return barrierAdaptR; } // 0=チップ型(減衰率重視)、1=ヘビー型(容量重視)

    public void recordBarrierHit(float rawDamage, boolean barrierBroke, long currentTick){
        totalDamageEncountered += rawDamage;

        double heavyRatio = BalanceConfig.BARRIER_HEAVY_HIT_THRESHOLD_RATIO.get();
        boolean isHeavy = barrierBroke
                || (baseMaxBarrierPoint > 0 && rawDamage >= baseMaxBarrierPoint * heavyRatio);

        if (isHeavy) {
            heavyHitScore += 1.0;
        } else if (lastBarrierHitTick >= 0 && (currentTick - lastBarrierHitTick) < BalanceConfig.BARRIER_CHIP_INTERVAL_TICKS.get()) {
            chipHitScore += 1.0;
        }
        lastBarrierHitTick = currentTick;

        double rInstant = heavyHitScore / (heavyHitScore + chipHitScore + BARRIER_EPSILON);
        double alpha = BalanceConfig.BARRIER_ADAPT_ALPHA.get();
        barrierAdaptR = barrierAdaptR * (1.0 - alpha) + rInstant * alpha;
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