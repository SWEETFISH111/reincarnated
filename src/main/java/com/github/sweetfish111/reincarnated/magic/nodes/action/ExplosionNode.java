package com.github.sweetfish111.reincarnated.magic.nodes.action;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class ExplosionNode extends AbstractMagicNode {
    float BASECOST = 5;

    @Override
    public void execute(MagicContext context) {
        double rawData = pullDouble(2,context);
        masoCost = BASECOST * (float) rawData;
        super.execute(context);
        BlockPos targetPos = BlockPos.containing(pullVector3(1, context));

        if(context.getCaster().level() instanceof ServerLevel serverLevel){
            float explosionPower = (float)rawData;

            serverLevel.explode(
                    context.getCaster(),
                    targetPos.getX(), targetPos.getY(), targetPos.getZ(),
                    explosionPower,
                    Level.ExplosionInteraction.TNT
            );
        }
        pushExecute(context);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        return null;
    }
}
