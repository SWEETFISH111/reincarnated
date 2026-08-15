package com.github.sweetfish111.reincarnated.magic.skill.node.conversion;

import com.github.sweetfish111.reincarnated.magic.KillScoreAmount;
import com.github.sweetfish111.reincarnated.magic.MasoAmount;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;

import java.util.UUID;

public class ConbersKillToMaso extends AbstractMagicNode {
    private static final double CONVERSE_RATE = 0.5;

    public ConbersKillToMaso(UUID id) {
        super(id);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        context.incrementAndCheck();
        KillScoreAmount killScore = pullKillScore(0, context);
        return new MasoAmount(killScore.amount() * CONVERSE_RATE);
    }
}
