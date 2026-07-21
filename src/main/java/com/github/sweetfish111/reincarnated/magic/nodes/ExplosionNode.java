package com.github.sweetfish111.reincarnated.magic.nodes;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class ExplosionNode extends AbstractMagicNode{
    float MAX_EXPLOSION_POWER = 10.0f;

    @Override
    public void execute(MagicContext context) {
        BlockPos targetPos = BlockPos.containing(pullVector3(1, context));

        if(context.getCaster().level() instanceof ServerLevel serverLevel){
            double rawData = pullDouble(2,context);
            float explosionPower = Math.min(MAX_EXPLOSION_POWER, (float)rawData);

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
