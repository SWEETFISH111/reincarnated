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

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        context.incrementAndCheck();
        XpAmount xp = pullXp(0, context);
        double converseRate = 0.2;
        return new MasoAmount(xp.amount() * converseRate);
    }
}
