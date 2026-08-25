package com.github.sweetfish111.reincarnated.magic.nodes.tank;

import com.github.sweetfish111.reincarnated.magic.PoolType;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.mojang.blaze3d.audio.Library;

import java.util.UUID;

public class DepositToTankNode extends AbstractMagicNode {
    public DepositToTankNode(UUID id) {
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
        super.execute(context);
        Object rawData = pullData(1, context);
        if(rawData instanceof PoolType p){
            double request = pullDouble(2, context);
            double availableAmount = p.consumeMaso(context, request);
            context.getMasoTank().deposit(availableAmount);
            /*
            if(MasoOverloadHandler.attemptOverload(context, overflowAmount)){
                context.getOverLoadBuff().arm(bonus)
            }
            todo overload処理
             */
        }
        pushExecute(context);
    }
}
