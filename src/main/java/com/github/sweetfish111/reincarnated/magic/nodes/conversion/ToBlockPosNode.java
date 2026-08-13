package com.github.sweetfish111.reincarnated.magic.nodes.conversion;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class ToBlockPosNode extends AbstractMagicNode {
    public ToBlockPosNode(UUID id) {
        super(id);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(portIndex, context);
        Vec3 rawPos = pullVector3(0, context);
        if(rawPos != null){
            BlockPos blockPos = BlockPos.containing(rawPos);
            return Vec3.atCenterOf(blockPos);
        }
        return null;
    }
}
