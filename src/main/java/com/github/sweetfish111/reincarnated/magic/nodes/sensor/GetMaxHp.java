package com.github.sweetfish111.reincarnated.magic.nodes.sensor;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

public class GetMaxHp extends AbstractMagicNode {
    public GetMaxHp(UUID id) {
        super(id);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(portIndex, context);
        Object rawData = pullData(0, context);
        if(rawData instanceof LivingEntity entity){
            return entity.getMaxHealth();
        }
        return 0;
    }
}
