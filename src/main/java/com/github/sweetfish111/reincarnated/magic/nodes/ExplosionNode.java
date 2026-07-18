package com.github.sweetfish111.reincarnated.magic.nodes;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class ExplosionNode extends AbstractMagicNode{
    @Override
    public void execute(MagicContext context) {
        BlockPos targetPos = pullBlockPos(1, context);

        if(targetPos != null && context.getCaster().level() instanceof ServerLevel serverLevel){
            float explosionPower = 4.0f;
            serverLevel.explode(
                    context.getCaster(),
                    targetPos.getX(), targetPos.getY(), targetPos.getZ(),
                    explosionPower,
                    Level.ExplosionInteraction.TNT
            );
        }
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        return null;
    }
}
