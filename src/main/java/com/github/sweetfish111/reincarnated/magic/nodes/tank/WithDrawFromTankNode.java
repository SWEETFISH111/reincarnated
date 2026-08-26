package com.github.sweetfish111.reincarnated.magic.nodes.tank;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.magic.record.MasoAmount;

import java.util.UUID;

public class WithDrawFromTankNode extends AbstractMagicNode {
    public WithDrawFromTankNode(UUID id) {
        super(id);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(portIndex, context);
        double request = pullDouble(0, context);
        double available = context.getMasoTank().getBalance();
        if(available >= request){
            return new MasoAmount(request);
        }else {
            return new MasoAmount(available);
        }
    }
}
