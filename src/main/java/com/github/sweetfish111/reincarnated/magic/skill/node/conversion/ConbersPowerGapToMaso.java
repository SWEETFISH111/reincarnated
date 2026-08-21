package com.github.sweetfish111.reincarnated.magic.skill.node.conversion;

import com.github.sweetfish111.reincarnated.magic.record.MasoAmount;
import com.github.sweetfish111.reincarnated.magic.record.PowerGapAmount;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;

import java.util.UUID;

/**
 * 「簒奪者(usurper)」ルート用：OnAttackStrongerNodeのpowerGap（相手との攻撃力差）を魔素に変換する。
 * powerGapは理論上常に正の値だが、念のため負値は0にクランプしておく。
 */
public class ConbersPowerGapToMaso extends AbstractMagicNode {
    private static final double CONVERSE_RATE = 0.3;

    public ConbersPowerGapToMaso(UUID id) {
        super(id);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        context.incrementAndCheck();
        PowerGapAmount powerGap = pullPowerGap(0, context);
        double powerGapAmount = Math.max(0.0, powerGap.amount());
        return new MasoAmount(powerGapAmount * CONVERSE_RATE);
    }
}
