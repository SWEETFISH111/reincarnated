package com.github.sweetfish111.reincarnated.magic.nodes.tank;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.magic.record.MasoAmount;
import com.github.sweetfish111.reincarnated.magic.tank.MasoTank;

import java.util.UUID;

public class MasoTankNode extends AbstractMagicNode {
    public MasoTankNode(UUID id) {
        super(id);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(portIndex, context);
        MasoTank masoTank = context.getMasoTank();
        double available = context.getMasoTank().getBalance();
        double bonus = context.getOverLoadBuff().consumeIfAvailable();
        return new MasoAmount(available * bonus);
    }
}
