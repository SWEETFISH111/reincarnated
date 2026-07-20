package com.github.sweetfish111.reincarnated.magic.nodes;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class GetLookForwardNode extends AbstractMagicNode{

    public GetLookForwardNode(UUID nodeId) {
        super(nodeId);
    }

    @Override
    public void execute(MagicContext context) {
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        if(portIndex == 0){
            Object distanceData = pullData(0, context);
            double maxDistance = distanceData instanceof Number num ? num.doubleValue() : 16.0;

            Vec3 eyePos = context.getCaster().getEyePosition();
            Vec3 lookVec = context.getCaster().getLookAngle();
            Vec3 endPos = eyePos.add(lookVec.scale(maxDistance));

            if(context.getCircuit().getNodeParam(this.id, "value", false) instanceof Boolean b){
                BlockHitResult blockHit = context.getCaster().level().clip(new ClipContext(eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, context.getCaster()));
                if(blockHit.getType() == HitResult.Type.BLOCK && b){
                    System.out.println("inner1");
                    return blockHit.getBlockPos();
                }
                System.out.println("outer1");
            }
            return endPos;
        }
        return null;
    }
}
