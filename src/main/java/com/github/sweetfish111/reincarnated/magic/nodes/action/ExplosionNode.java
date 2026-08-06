package com.github.sweetfish111.reincarnated.magic.nodes.action;

import com.github.sweetfish111.reincarnated.magic.caster.PlayerCasterAdapter;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class ExplosionNode extends AbstractMagicNode {
    float BASECOST = 4;

    public ExplosionNode(UUID id){
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
        double rawData = pullDouble(2,context);
        masoCost = BASECOST * (float) rawData;
        super.execute(context);
        BlockPos targetPos = BlockPos.containing(pullVector3(1, context));
        Level.ExplosionInteraction interaction = Level.ExplosionInteraction.NONE;
        if (context.getCircuit() != null) {
            Object switchBoolean = context.getCircuit().getNodeParam(this.id, "value", false);
            if (switchBoolean instanceof Boolean b && b) {
                interaction = Level.ExplosionInteraction.TNT;
            }
        }
        if(context.getCaster().getCasterLevel() instanceof ServerLevel serverLevel){
            float explosionPower = (float)rawData;
            Entity sourceEntity = context.getCaster().getCasterEntity();
            serverLevel.explode(
                    sourceEntity,
                    targetPos.getX(), targetPos.getY(), targetPos.getZ(),
                    explosionPower,
                    interaction
            );
        }
        pushExecute(context);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        return null;
    }
}
