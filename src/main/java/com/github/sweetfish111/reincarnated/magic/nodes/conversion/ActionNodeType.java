package com.github.sweetfish111.reincarnated.magic.nodes.conversion;

import com.github.sweetfish111.reincarnated.config.BalanceConfig;

import java.util.function.Supplier;

/**
 * GetBaseCostNodeが「どのアクションノードの基礎コストを取得するか」を選ぶための列挙。
 * SummonBehavior（SummonNodeの振る舞い選択）と同じ「数値→enum」パターンを踏襲しており、
 * MagiculeNodeType（enum名と表示名/id文字列の食い違いが多い）とは完全に独立している。
 * <p>
 * 将来アクションノードを追加する際は、ここに列挙を1つ足すだけでよい。
 */
public enum ActionNodeType {
    DAMAGE(0, () -> BalanceConfig.DAMAGE_BASE_COST),
    HEALING(1, () -> BalanceConfig.HEALING_BASE_COST),
    EXPLOSION(2, () -> BalanceConfig.EXPLOSION_BASE_COST),
    DIG(3, () -> BalanceConfig.DIG_BASE_COST),
    COLLECT_ITEMS(4, () -> BalanceConfig.COLLECT_ITEMS_BASE_COST),
    SUMMON(5, () -> BalanceConfig.SUMMON_BASE_COST);

    private final int id;
    private final Supplier<net.neoforged.neoforge.common.ModConfigSpec.DoubleValue> configValue;

    ActionNodeType(int id, Supplier<net.neoforged.neoforge.common.ModConfigSpec.DoubleValue> configValue) {
        this.id = id;
        this.configValue = configValue;
    }

    public int getId() {
        return id;
    }

    public float getBaseCost() {
        return configValue.get().get().floatValue();
    }

    public static ActionNodeType fromId(int id) {
        for (ActionNodeType type : values()) {
            if (type.id == id) return type;
        }
        return DAMAGE;
    }
}
