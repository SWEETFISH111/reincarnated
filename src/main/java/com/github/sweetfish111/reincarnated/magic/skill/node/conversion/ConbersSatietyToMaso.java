package com.github.sweetfish111.reincarnated.magic.skill.node.conversion;

import com.github.sweetfish111.reincarnated.magic.MasoAmount;
import com.github.sweetfish111.reincarnated.magic.SatietyAmount;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;

import java.util.UUID;

/**
 * 「飢餓者(scavenger)」ルート用：OnEatNodeのsatietyLevelを魔素に変換する。
 */
public class ConbersSatietyToMaso extends AbstractMagicNode {
    private static final double CONVERSE_RATE = 0.5;

    public ConbersSatietyToMaso(UUID id) {
        super(id);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        context.incrementAndCheck();
        SatietyAmount satiety = pullSatiety(0, context);
        return new MasoAmount(satiety.amount() * CONVERSE_RATE);
    }
}
