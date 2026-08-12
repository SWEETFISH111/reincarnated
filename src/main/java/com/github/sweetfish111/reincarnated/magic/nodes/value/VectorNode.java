package com.github.sweetfish111.reincarnated.magic.nodes.value;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class VectorNode extends AbstractMagicNode {
    public VectorNode(UUID id) {
        super(id);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(portIndex, context);
        double x = pullDouble(0, context);
        double y = pullDouble(1, context);
        double z = pullDouble(2, context);
        return new Vec3(x, y, z);
    }
}
