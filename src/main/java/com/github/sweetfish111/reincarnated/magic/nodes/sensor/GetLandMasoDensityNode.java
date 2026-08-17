package com.github.sweetfish111.reincarnated.magic.nodes.sensor;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.world.LandMasoDensityData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class GetLandMasoDensityNode extends AbstractMagicNode {
    public GetLandMasoDensityNode(UUID id) {
        super(id);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(portIndex, context);

        Vec3 pos = pullVector3(0, context);
        if (pos == null) return 0.0;

        if (context.getLevel() instanceof ServerLevel serverLevel) {
            return (double) LandMasoDensityData.get(serverLevel).getDensity(serverLevel, BlockPos.containing(pos));
        }
        return 0.0;
    }
}