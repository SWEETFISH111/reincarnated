package com.github.sweetfish111.reincarnated.magic.nodes;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class OffsetNode extends AbstractMagicNode{
    @Override
    public void execute(MagicContext context) {
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        Vec3 sourcePos = pullVector3(0, context);
        double offsetX = (double) pullData(1, context);
        double offsetY = (double) pullData(2, context);
        double offsetZ = (double) pullData(3, context);
        Vec3 offset = new Vec3(offsetX, offsetY, offsetZ);
        Vec3 CalculatedPos = sourcePos.add(offset);
        System.out.println("******" + CalculatedPos +"******");
        return CalculatedPos;
    }
}
