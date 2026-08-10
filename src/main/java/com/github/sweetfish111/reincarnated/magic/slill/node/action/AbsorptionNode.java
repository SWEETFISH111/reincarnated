package com.github.sweetfish111.reincarnated.magic.slill.node.action;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class AbsorptionNode extends AbstractMagicNode {
    float BASECOST = 0.01f;
    float RATE = 0.1f;

    public AbsorptionNode(UUID id) {
        super(id);
        masoCost = BASECOST;
    }

    @Override
    public void execute(MagicContext context) {
        if(context.getCaster().getCasterEntity() instanceof ServerPlayer player){
            float amount = (float) pullDouble(1, context) * RATE;
            float currentAbsorption = player.getAbsorptionAmount();
            float newAbsorption = currentAbsorption + amount;

            ensureMaxAbsorption(player, newAbsorption);

            player.setAbsorptionAmount(newAbsorption);

            super.execute(context);
        }
    }
}
