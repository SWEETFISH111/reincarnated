package com.github.sweetfish111.reincarnated.magic.slill.node.conversion;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.magic.MasoAmount;
import com.github.sweetfish111.reincarnated.magic.XpAmount;

import java.util.UUID;

public class ConbersXpToMaso extends AbstractMagicNode {
    public ConbersXpToMaso(UUID id) {
        super(id);
    }
    private final double conversRate = 1d / 20d;

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        context.incrementAndCheck();
        XpAmount xp = pullXp(0, context);
        return new MasoAmount(xp.xpAmount() * conversRate);
    }
}
