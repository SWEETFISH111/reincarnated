package com.github.sweetfish111.reincarnated.player;

import com.github.sweetfish111.reincarnated.config.BalanceConfig;
import net.minecraft.nbt.CompoundTag;

public class ComputeCapacity implements PersistentComponent {
    private double totalCastTimeSpent = 0.0; // 累計詠唱tick数

    public void recordCastTime(double castTimeTicks){
        totalCastTimeSpent += Math.max(0, castTimeTicks);
    }

    public double getTotalCastTimeSpent(){ return totalCastTimeSpent; }

    /**
     * 演算能力の最大値。進化ステージが成長率そのものを強く底上げする
     * （同じ詠唱経験でも、進化が進んでいるほど演算能力の伸びが大きい）。
     */
    public double getMaxComputeCapacity(MasoEvolutionStage currentStage){
        double stageMultiplier = Math.pow(currentStage.ordinal() + 1, BalanceConfig.COMPUTE_CAPACITY_STAGE_EXPONENT.get());
        double growth = Math.log(1.0 + totalCastTimeSpent / BalanceConfig.COMPUTE_CAPACITY_DIVISOR.get());
        return BalanceConfig.BASE_COMPUTE_CAPACITY.get() + stageMultiplier * BalanceConfig.COMPUTE_CAPACITY_SCALE.get() * growth;
    }

    @Override
    public void saveToNBT(CompoundTag tag) {
        tag.putDouble("totalCastTimeSpent", totalCastTimeSpent);
    }

    @Override
    public void loadFromNBT(CompoundTag tag) {
        totalCastTimeSpent = tag.getDouble("totalCastTimeSpent").orElse(0.0);
    }
}