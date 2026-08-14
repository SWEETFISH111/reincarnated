package com.github.sweetfish111.reincarnated.player;

import net.minecraft.nbt.CompoundTag;

/**
 * 魔素経済（現在値・消費/回復の累積・進化ステージ）を担当するコンポーネント。
 * NBTタグ構造は元の"maso"タグと完全互換。
 */
public class MasoEconomy implements PersistentComponent {
    private float currentMaso = 20f;
    private float totalRegeneratedMaso = 0f;
    private float totalConsumedMaso = 0f;

    private MasoEvolutionStage masoStage = MasoEvolutionStage.STAGE0;
    private double stageStartConsumedMaso = 0.0;
    private double stageStartRegeneratedMaso = 0.0;
    private boolean pendingMasoEvolutionTrigger = false;

    private static final double STAGE_CARRYOVER_RATIO = 0.3;
    private static final double REGEN_RECOVERY_MIDPOINT_RATIO = 0.5;
    public static final double MASO_SCALE_DIVISOR = 250.0;

    public float getCurrentMaso(){ return currentMaso; }
    public void setCurrentMaso(float value){ this.currentMaso = value; }
    public void addCurrentMaso(float amount){ this.currentMaso += amount; }

    public float getTotalRegeneratedMaso(){ return totalRegeneratedMaso; }
    public void addTotalRegeneratedMaso(float amount){ this.totalRegeneratedMaso += amount; }

    public float getTotalConsumedMaso(){ return totalConsumedMaso; }

    /** 魔素を消費する（現在値を減らし、累積消費量も加算）。旧PlayerCasterAdapterの直接フィールド操作の代替。 */
    public void consumeMaso(float amount){
        this.currentMaso -= amount;
        this.totalConsumedMaso += amount;
    }

    public MasoEvolutionStage getMasoStage(){ return masoStage; }

    public float getMaxMaso(){
        advanceMasoStageIfNeeded();
        double sinceStageStart = Math.max(0.0, this.totalConsumedMaso - stageStartConsumedMaso);
        double scaledInput = sinceStageStart / MASO_SCALE_DIVISOR;
        return (float) (masoStage.getFloor() + masoStage.getScaleFactor() * Math.log(1.0 + scaledInput));
    }

    public float getMasoRegenRate(){
        double sinceStageStart = Math.max(0.0, this.totalRegeneratedMaso - stageStartRegeneratedMaso);
        double scaledInput = sinceStageStart / MASO_SCALE_DIVISOR;
        return (float)(masoStage.getRegenFloor() + masoStage.getRegenScaleFactor() * Math.log(1.0 + scaledInput));
    }

    public void triggerMasoStageEvolutionAttempt(){
        this.pendingMasoEvolutionTrigger = true;
        advanceMasoStageIfNeeded();
    }

    private void advanceMasoStageIfNeeded(){
        if (!pendingMasoEvolutionTrigger) return;

        MasoEvolutionStage next = masoStage.getNext();
        if (next == null) {
            pendingMasoEvolutionTrigger = false;
            return;
        }

        double sinceStageStart = totalConsumedMaso - stageStartConsumedMaso;
        double threshold = masoStage.getEvolutionThreshold();
        if (sinceStageStart < threshold) {
            return;
        }

        double overflow = sinceStageStart - threshold;
        double carryHeadStart = overflow * STAGE_CARRYOVER_RATIO;

        double prevFinalRegenRate = getMasoRegenRate();

        masoStage = next;
        stageStartConsumedMaso = totalConsumedMaso - carryHeadStart;

        double newFloor = masoStage.getRegenFloor();
        double newScaleFactor = masoStage.getRegenScaleFactor();
        if (prevFinalRegenRate > newFloor) {
            double targetRate = newFloor + (prevFinalRegenRate - newFloor) * REGEN_RECOVERY_MIDPOINT_RATIO;
            double regenHeadStart = 100.0 * (Math.exp((targetRate - newFloor) / newScaleFactor) - 1.0);
            stageStartRegeneratedMaso = totalRegeneratedMaso - regenHeadStart;
        } else {
            stageStartRegeneratedMaso = totalRegeneratedMaso;
        }
        pendingMasoEvolutionTrigger = false;

        onMasoStageEvolved(masoStage);
    }

    private void onMasoStageEvolved(MasoEvolutionStage newStage) {
        // TODO: 進化演出・実績通知・ネットワーク同期パケット送信などをここに実装
    }

    @Override
    public void saveToNBT(CompoundTag tag) {
        tag.putFloat("currentMaso", currentMaso);
        tag.putFloat("totalRegeneratedMaso", totalRegeneratedMaso);
        tag.putFloat("totalConsumedMaso", totalConsumedMaso);
        tag.putInt("masoStage", masoStage.ordinal());
        tag.putDouble("stageStartConsumedMaso", stageStartConsumedMaso);
        tag.putDouble("stageStartRegeneratedMaso", stageStartRegeneratedMaso);
        tag.putBoolean("pendingMasoEvolutionTrigger", pendingMasoEvolutionTrigger);
    }

    @Override
    public void loadFromNBT(CompoundTag tag) {
        currentMaso = tag.getFloat("currentMaso").orElse(20f);
        totalRegeneratedMaso = tag.getFloat("totalRegeneratedMaso").orElse(0f);
        totalConsumedMaso = tag.getFloat("totalConsumedMaso").orElse(0f);
        masoStage = MasoEvolutionStage.fromIndex(tag.getInt("masoStage").orElse(0));
        stageStartConsumedMaso = tag.getDouble("stageStartConsumedMaso").orElse(0.0);
        stageStartRegeneratedMaso = tag.getDouble("stageStartRegeneratedMaso").orElse(0.0);
        pendingMasoEvolutionTrigger = tag.getBoolean("pendingMasoEvolutionTrigger").orElse(false);
    }
}