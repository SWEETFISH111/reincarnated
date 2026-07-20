package com.github.sweetfish111.reincarnated.magic.nodes;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class OffsetNode extends AbstractMagicNode{
    public OffsetNode(UUID id){
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        Object rawParam = context.getCircuit().getNodeParam(this.id, "value", "【データが存在しません】");
        System.out.println("★ [OffsetNode Check] rawParam = " + rawParam + " (Node ID: " + this.id + ")");

        boolean isModeActive = false;
        Object param = context.getCircuit().getNodeParam(this.id, "value", true);
        if(param instanceof Boolean b){
            isModeActive = b;
        }
        if(isModeActive){
            Vec3 basePos = pullVector3(0, context);
            Vec3 targetDirection = pullVector3(1, context);
            Vec3 upVec = new Vec3(0, 1, 0);
            Vec3 rightVec = targetDirection.cross(upVec).normalize();

            Vec3 actualUpVec = rightVec.cross(targetDirection).normalize();

            Vec3 offsetVector = targetDirection.scale(pullDouble(2, context))
                    .add(actualUpVec.scale(pullDouble(3, context)))
                    .add(rightVec.scale(pullDouble(4, context)));
            return basePos.add(offsetVector);
        }else{
            Vec3 sourcePos = pullVector3(0, context);
            double offsetX = pullDouble(1, context);
            double offsetY = pullDouble(2, context);
            double offsetZ = pullDouble(3, context);
            Vec3 offset = new Vec3(offsetX, offsetY, offsetZ);
            Vec3 CalculatedPos = sourcePos.add(offset);
            return CalculatedPos;
        }
    }
}
