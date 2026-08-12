package com.github.sweetfish111.reincarnated.magic.nodes.sensor;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class GetLookForwardNode extends AbstractMagicNode {

    public GetLookForwardNode(UUID nodeId) {
        super(nodeId);
    }

    @Override
    public void execute(MagicContext context) {
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(portIndex, context);
        Object distanceData = pullData(0, context);
        double maxDistance = distanceData instanceof Number num ? num.doubleValue() : 16.0;

        Vec3 eyePos = context.getCaster().getCasterEntity().getEyePosition();
        Vec3 lookVec = context.getCaster().getCasterEntity().getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.scale(maxDistance));

        if(portIndex == 0){
            if(context.getCircuit().getNodeParam(this.id, "value", false) instanceof Boolean b){
                BlockHitResult blockHit = context.getCaster().getCasterLevel().clip(new ClipContext(eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, context.getCaster().getCasterEntity()));
                if(blockHit.getType() == HitResult.Type.BLOCK && b){
                    return blockHit.getBlockPos();
                }
            }
            return endPos;
        }

        if(context.getCircuit().getNodeParam(this.id, "value", false) instanceof Boolean b){
            if(portIndex == 1){
                BlockHitResult blockHit = context.getCaster().getCasterLevel().clip(new ClipContext(eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, context.getCaster().getCasterEntity()));
                if(blockHit.getType() == HitResult.Type.BLOCK && b){
                    Direction face = blockHit.getDirection();
                    Vec3 surfaceNormal = new Vec3(
                            face.getStepX(),
                            face.getStepY(),
                            face.getStepZ()
                    );
                    return surfaceNormal;
                }
            }
        }
        return null;
    }
}
