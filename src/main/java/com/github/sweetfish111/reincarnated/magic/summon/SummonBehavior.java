package com.github.sweetfish111.reincarnated.magic.summon;

/**
 * 召喚物の振る舞いモード。将来的にAGGRESSIVE等を追加する際もここに列挙を足すだけでいい。
 */
public enum SummonBehavior {
    IDLE(0),   // その場に留まるだけ
    DECOY(1);  // 術者の視線方向を模倣する（デコイ）

    private final int id;
    SummonBehavior(int id) { this.id = id; }
    public int getId() { return id; }

    public static SummonBehavior fromId(int id) {
        for (SummonBehavior b : values()) {
            if (b.id == id) return b;
        }
        return IDLE;
    }
}