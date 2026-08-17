package com.github.sweetfish111.reincarnated.player;

import com.github.sweetfish111.reincarnated.config.BalanceConfig;
import net.minecraft.nbt.CompoundTag;

public class MasoEconomy implements PersistentComponent {
    private float currentMaso = 20f;
    private float totalRegeneratedMaso = 0f;
    private float totalConsumedMaso = 0f;

    private MasoEvolutionStage masoStage = MasoEvolutionStage.STAGE0;
    private double stageStartConsumedMaso = 0.0;
    private double stageStartRegeneratedMaso = 0.0;
    private boolean pendingMasoEvolutionTrigger = false;

    private double burstScore = 0.0;
    private double sustainScore = 0.0;
    private double rSmoothed = 0.5;
    private long lastCastTick = -1;

    private static final double STYLE_EPSILON = 0.001; // ゼロ除算防止のみ、チューニング対象外なので残す

    public float getCurrentMaso(){ return currentMaso; }
    public void setCurrentMaso(float value){ this.currentMaso = value; }
    public void addCurrentMaso(float amount){ this.currentMaso += amount; }

    public float getTotalRegeneratedMaso(){ return totalRegeneratedMaso; }
    public void addTotalRegeneratedMaso(float amount){ this.totalRegeneratedMaso += amount; }

    public float getTotalConsumedMaso(){ return totalConsumedMaso; }

    public void consumeMaso(float amount, long currentTick){
        this.currentMaso -= amount;
        this.totalConsumedMaso += amount;
        recordCastForStyle(amount, currentTick);
    }

    public void consumePassive(float amount) {
        this.currentMaso -= amount;
        this.totalConsumedMaso += amount;
    }

    private void recordCastForStyle(float consumedAmount, long currentTick){
        advanceMasoStageIfNeeded();
        double baseMax = getBaseMaxMaso();
        double ratio = baseMax > 0 ? consumedAmount / baseMax : 0;

        double burstThreshold = BalanceConfig.BURST_RATIO_THRESHOLD.get();
        if (ratio >= burstThreshold) {
            burstScore += (ratio - burstThreshold) * 2.0;
        } else if (lastCastTick >= 0 && (currentTick - lastCastTick) < BalanceConfig.SUSTAIN_INTERVAL_TICKS.get()) {
            sustainScore += 1.0;
        }
        lastCastTick = currentTick;

        double rInstant = burstScore / (burstScore + sustainScore + STYLE_EPSILON);
        double alpha = BalanceConfig.STYLE_ALPHA.get();
        rSmoothed = rSmoothed * (1 - alpha) + rInstant * alpha;
    }

    public double getStylePreference(){ return rSmoothed; }

    private double getStyleGrowth(){
        return Math.log(1.0 + totalConsumedMaso / BalanceConfig.STYLE_GROWTH_DIVISOR.get());
    }

    public MasoEvolutionStage getMasoStage(){ return masoStage; }

    private double getBaseMaxMaso(){
        double sinceStageStart = Math.max(0.0, this.totalConsumedMaso - stageStartConsumedMaso);
        double scaledInput = sinceStageStart / BalanceConfig.MASO_SCALE_DIVISOR.get();
        return masoStage.getFloor() + masoStage.getScaleFactor() * Math.log(1.0 + scaledInput);
    }

    public float getMaxMaso(){
        advanceMasoStageIfNeeded();
        double styleKMax = BalanceConfig.STYLE_K_REGEN.get() * BalanceConfig.STYLE_REFERENCE_T_TICKS.get();
        double styleBonus = getStyleGrowth() * rSmoothed * styleKMax;
        return (float) (getBaseMaxMaso() + styleBonus);
    }

    public float getMasoRegenRate(){
        double sinceStageStart = Math.max(0.0, this.totalRegeneratedMaso - stageStartRegeneratedMaso);
        double scaledInput = sinceStageStart / BalanceConfig.MASO_SCALE_DIVISOR.get();
        double baseValue = masoStage.getRegenFloor() + masoStage.getRegenScaleFactor() * Math.log(1.0 + scaledInput);

        double styleBonus = getStyleGrowth() * (1 - rSmoothed) * BalanceConfig.STYLE_K_REGEN.get();
        return (float)(baseValue + styleBonus);
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
        double carryHeadStart = overflow * BalanceConfig.STAGE_CARRYOVER_RATIO.get();

        double prevFinalRegenRate = getMasoRegenRate();

        masoStage = next;
        stageStartConsumedMaso = totalConsumedMaso - carryHeadStart;

        double newFloor = masoStage.getRegenFloor();
        double newScaleFactor = masoStage.getRegenScaleFactor();
        if (prevFinalRegenRate > newFloor) {
            double midpointRatio = BalanceConfig.REGEN_RECOVERY_MIDPOINT_RATIO.get();
            double targetRate = newFloor + (prevFinalRegenRate - newFloor) * midpointRatio;
            double regenHeadStart = 100.0 * (Math.exp((targetRate - newFloor) / newScaleFactor) - 1.0);
            stageStartRegeneratedMaso = totalRegeneratedMaso - regenHeadStart;
        } else {
            stageStartRegeneratedMaso = totalRegeneratedMaso;
        }
        pendingMasoEvolutionTrigger = false;

        onMasoStageEvolved(masoStage);
    }

    private MasoEvolutionStage pendingStageEvolutionNotification = null;

    private void onMasoStageEvolved(MasoEvolutionStage newStage) {
        this.pendingStageEvolutionNotification = newStage;
    }

    /**
     * 段階進化が発生していれば、その段階を返して通知を消費する（一度きり）。
     * 発生していなければnull。NBT永続化は不要（1tick以内に消費される想定の一時フラグ）。
     */
    public MasoEvolutionStage pollStageEvolutionEvent(){
        MasoEvolutionStage result = pendingStageEvolutionNotification;
        pendingStageEvolutionNotification = null;
        return result;
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
        tag.putDouble("burstScore", burstScore);
        tag.putDouble("sustainScore", sustainScore);
        tag.putDouble("rSmoothed", rSmoothed);
        tag.putDouble("lastCastTick", (double) lastCastTick);
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
        burstScore = tag.getDouble("burstScore").orElse(0.0);
        sustainScore = tag.getDouble("sustainScore").orElse(0.0);
        rSmoothed = tag.getDouble("rSmoothed").orElse(0.5);
        lastCastTick = (long)(double) tag.getDouble("lastCastTick").orElse(-1.0);
    }
}