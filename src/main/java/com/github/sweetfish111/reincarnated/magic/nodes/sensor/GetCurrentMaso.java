package com.github.sweetfish111.reincarnated.magic.nodes.sensor;

import com.github.sweetfish111.reincarnated.init.ReincarnatedAttachments;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.player.PlayerMagicData;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class GetCurrentMaso extends AbstractMagicNode {
    public GetCurrentMaso(UUID id) {
        super(id);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(portIndex, context);
        Object rawData = pullData(0, context);
        if(rawData instanceof ServerPlayer player){
            PlayerMagicData magicData = player.getData(ReincarnatedAttachments.PLAYER_MAGIC_DATA);
            return magicData.getCurrentMaso();
        }
        return 0;
    }
}
