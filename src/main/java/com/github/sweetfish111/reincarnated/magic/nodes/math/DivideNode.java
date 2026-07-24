package com.github.sweetfish111.reincarnated.magic.nodes.math;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import net.minecraft.world.phys.Vec3;

public class DivideNode extends AbstractMagicNode {
    @Override
    public void execute(MagicContext context) {
        //なにもしない
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        Object valA = pullData(0, context);
        Object valB = pullData(1, context);

        if (valA instanceof Number a && valB instanceof Number b) {
            return b.doubleValue() != 0 ? (a.doubleValue() / b.doubleValue()) : 0;
        } else if (valA instanceof Vec3 a && valB instanceof Number b) {
            return b.doubleValue() != 0 ? (a.scale(1 / b.doubleValue())) : 0;
        }
        return 0;
    }
}
