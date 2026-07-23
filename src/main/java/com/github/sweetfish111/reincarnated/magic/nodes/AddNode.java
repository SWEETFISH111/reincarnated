package com.github.sweetfish111.reincarnated.magic.nodes;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import net.minecraft.world.phys.Vec3;

public class AddNode extends AbstractMagicNode{
    @Override
    public void execute(MagicContext context) {
        //なにもしない
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        Object valA = pullData(0, context);
        Object valB = pullData(1, context);

        if (valA instanceof Number a && valB instanceof Number b) {
            return a.doubleValue() + b.doubleValue();
        } else if (valA instanceof Vec3 a && valB instanceof Vec3 b) {
            return a.add(b);
        }
        return 0;
    }
}
