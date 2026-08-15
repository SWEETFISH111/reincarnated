package com.github.sweetfish111.reincarnated.player;

import net.minecraft.nbt.CompoundTag;

public class MasoEconomy implements PersistentComponent {
    private float currentMaso = 20f;
    private float totalRegeneratedMaso = 0f;
    private float totalConsumedMaso = 0f;

    private MasoEvolutionStage masoStage = MasoEvolutionStage.STAGE0;
    private double stageStartConsumedMaso = 0.0;
    private double stageStartRegeneratedMaso = 0.0;
    private boolean pendingMasoEvolutionTrigger = false;

    // ★新規：バースト/継続プレイスタイル適応
    private double burstScore = 0.0;
    private double sustainScore = 0.0;
    private double rSmoothed = 0.5; // 0=完全継続型、1=完全バースト型
    private long lastCastTick = -1;

    private static final double STAGE_CARRYOVER_RATIO = 0.3;
    private static final double REGEN_RECOVERY_MIDPOINT_RATIO = 0.5;
    public static final double MASO_SCALE_DIVISOR = 250.0;

    // ★新規：スタイル適応のチューニング定数
    private static final double STYLE_EPSILON = 0.001;
    private static final double STYLE_ALPHA = 0.02;              // EMAの追従速度
    private static final double STYLE_GROWTH_DIVISOR = 250.0;
    private static final double STYLE_REFERENCE_T_TICKS = 400.0; // 等価性を保証する基準時間（20秒）
    private static final double STYLE_K_REGEN = 0.02;            // 校正の元になる回復側係数
    private static final double STYLE_K_MAX = STYLE_K_REGEN * STYLE_REFERENCE_T_TICKS; // k_max = k_regen×T
    private static final double BURST_RATIO_THRESHOLD = 0.5;     // 現在maxMasoの何割消費でバースト扱いか
    private static final int SUSTAIN_INTERVAL_TICKS = 100;       // 5秒以内の連続詠唱を継続扱い

    public float getCurrentMaso(){ return currentMaso; }
    public void setCurrentMaso(float value){ this.currentMaso = value; }
    public void addCurrentMaso(float amount){ this.currentMaso += amount; }

    public float getTotalRegeneratedMaso(){ return totalRegeneratedMaso; }
    public void addTotalRegeneratedMaso(float amount){ this.totalRegeneratedMaso += amount; }

    public float getTotalConsumedMaso(){ return totalConsumedMaso; }

    /**
     * 魔素を消費する（詠唱コストとしての消費）。
     * currentTickを渡すことで、消費のたびにバースト/継続の適応スコアも更新する。
     */
    public void consumeMaso(float amount, long currentTick){
        this.currentMaso -= amount;
        this.totalConsumedMaso += amount;
        recordCastForStyle(amount, currentTick);
    }

    private void recordCastForStyle(float consumedAmount, long currentTick){
        advanceMasoStageIfNeeded();
        double baseMax = getBaseMaxMaso();
        double ratio = baseMax > 0 ? consumedAmount / baseMax : 0;

        if (ratio >= BURST_RATIO_THRESHOLD) {
            burstScore += (ratio - BURST_RATIO_THRESHOLD) * 2.0;
        } else if (lastCastTick >= 0 && (currentTick - lastCastTick) < SUSTAIN_INTERVAL_TICKS) {
            sustainScore += 1.0;
        }
        lastCastTick = currentTick;

        double rInstant = burstScore / (burstScore + sustainScore + STYLE_EPSILON);
        rSmoothed = rSmoothed * (1 - STYLE_ALPHA) + rInstant * STYLE_ALPHA;
    }

    public double getStylePreference(){ return rSmoothed; } // 将来のステータス画面用

    private double getStyleGrowth(){
        return Math.log(1.0 + totalConsumedMaso / STYLE_GROWTH_DIVISOR);
    }

    public MasoEvolutionStage getMasoStage(){ return masoStage; }

    /** スタイルボーナスを含まない、進化ステージ由来の基礎最大魔素量（バースト判定の固定基準にも使う） */
    private double getBaseMaxMaso(){
        double sinceStageStart = Math.max(0.0, this.totalConsumedMaso - stageStartConsumedMaso);
        double scaledInput = sinceStageStart / MASO_SCALE_DIVISOR;
        return masoStage.getFloor() + masoStage.getScaleFactor() * Math.log(1.0 + scaledInput);
    }

    public float getMaxMaso(){
        advanceMasoStageIfNeeded();
        double styleBonus = getStyleGrowth() * rSmoothed * STYLE_K_MAX;
        return (float) (getBaseMaxMaso() + styleBonus);
    }

    public float getMasoRegenRate(){
        double sinceStageStart = Math.max(0.0, this.totalRegeneratedMaso - stageStartRegeneratedMaso);
        double scaledInput = sinceStageStart / MASO_SCALE_DIVISOR;
        double baseValue = masoStage.getRegenFloor() + masoStage.getRegenScaleFactor() * Math.log(1.0 + scaledInput);

        double styleBonus = getStyleGrowth() * (1 - rSmoothed) * STYLE_K_REGEN;
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