package com.github.sweetfish111.reincarnated.magic.nodes.sensor;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class GetBlockAtPos extends AbstractMagicNode {
    public GetBlockAtPos(UUID id) {
        super(id);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(0, context);
        Vec3 pos = pullVector3(0, context);
        if(pos != null){
            BlockPos blockPos = BlockPos.containing(pos.x, pos.y, pos.z);
            return context.getLevel().getBlockState(blockPos);
        }
        return null;
    }
}
