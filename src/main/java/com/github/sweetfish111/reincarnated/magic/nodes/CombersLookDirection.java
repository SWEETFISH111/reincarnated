package com.github.sweetfish111.reincarnated.magic.nodes;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import net.minecraft.world.phys.Vec3;

public class CombersLookDirection extends AbstractMagicNode{
    @Override
    public void execute(MagicContext context) {
        //なにもしない
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        return pullEntity(0, context).getLookAngle();
    }
}
