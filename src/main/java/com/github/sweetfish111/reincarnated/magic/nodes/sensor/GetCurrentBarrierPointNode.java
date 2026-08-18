package com.github.sweetfish111.reincarnated.magic.nodes.sensor;

import com.github.sweetfish111.reincarnated.init.ModAttachments;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class GetCurrentBarrierPointNode extends AbstractMagicNode {
    public GetCurrentBarrierPointNode(UUID id) {
        super(id);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(portIndex, context);
        Object rawData = pullData(0, context);
        if(rawData instanceof ServerPlayer player){
            return (double)player.getData(ModAttachments.PLAYER_MAGIC_DATA).getBarrierPoint();
        }
        return 0.0;
    }
}
