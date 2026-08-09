package com.github.sweetfish111.reincarnated.player;

/**
 * 魔素上限スケーリングの進化段階。
 * maxMaso = floor + scaleFactor * ln(1 + (totalConsumedMaso - stageStartConsumedMaso) / 100)
 *
 * evolutionThreshold は「このステージに入ってからの消費量(since-stage-start)」が
 * この値を超えたら次ステージへ進化する、というトリガー条件。
 * 各定数は暫定値。実プレイでの消費ペースを見ながらチューニングする前提（旧 MAX_SCALE_FACTOR=5.0 と同様）。
 */
public enum MasoEvolutionStage {
    // floor, scaleFactor, evolutionThreshold, displayName, regenFloor, regenScaleFactor
    //
    // STAGE0（旧世界）：オリジナルの旧式（floor=20, SF=5）をそのまま保持。
    //   遷移トリガーは魔素消費量ではなく checkEvolution() の greedyScore>=1 分岐
    //   （ノード解放が起きた瞬間＝「潜在能力の片鱗に気づく」）。
    //   このためevolutionThresholdは0（トリガーが立てば即座に遷移）。
    STAGE0(20.0, 5.0, 0.0, "旧世界（未覚醒）", 0.1, 0.5),
    // STAGE1（新ゼロ）〜STAGE3（新2）：以前設計した3段階をそのままスライド。
    STAGE1(20.0, 200.0, 5_000.0, "新世界・第零形態", 0.1, 0.5),
    STAGE2(800.0, 3_300.0, 150_000.0, "新世界・第一形態", 4.0, 20.0),
    STAGE3(25_000.0, 98_500.0, Double.MAX_VALUE, "新世界・第二形態（最終）", 160.0, 800.0);

    private final double floor;
    private final double scaleFactor;
    private final double evolutionThreshold;
    private final String displayName;
    private final double regenFloor;
    private final double regenScaleFactor;

    MasoEvolutionStage(double floor, double scaleFactor, double evolutionThreshold, String displayName,
                       double regenFloor, double regenScaleFactor) {
        this.floor = floor;
        this.scaleFactor = scaleFactor;
        this.evolutionThreshold = evolutionThreshold;
        this.displayName = displayName;
        this.regenFloor = regenFloor;
        this.regenScaleFactor = regenScaleFactor;
    }

    public double getFloor() { return floor; }
    public double getScaleFactor() { return scaleFactor; }
    public double getEvolutionThreshold() { return evolutionThreshold; }
    public String getDisplayName() { return displayName; }
    public double getRegenFloor() { return regenFloor; }
    public double getRegenScaleFactor() { return regenScaleFactor; }

    /** 次のステージ。最終段階では null。 */
    public MasoEvolutionStage getNext() {
        int next = this.ordinal() + 1;
        MasoEvolutionStage[] values = values();
        return next < values.length ? values[next] : null;
    }

    public static MasoEvolutionStage fromIndex(int index) {
        MasoEvolutionStage[] values = values();
        if (index < 0 || index >= values.length) return STAGE0;
        return values[index];
    }
}