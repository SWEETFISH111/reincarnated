package com.github.sweetfish111.reincarnated.magic.nodes.sensor;

import com.github.sweetfish111.reincarnated.init.ModAttachments;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class GetMaxBarrier extends AbstractMagicNode {
    public GetMaxBarrier(UUID id) {
        super(id);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(0, context);
        Object rawData = pullData(0, context);
        if(rawData instanceof ServerPlayer player){
            return player.getData(ModAttachments.PLAYER_MAGIC_DATA).getMaxBarrierPoint();
        }
        return 0.0;
    }
}
