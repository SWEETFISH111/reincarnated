package com.github.sweetfish111.reincarnated.magic.nodes.action;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

import java.util.UUID;


public class HealingNode extends AbstractMagicNode {
    float BASECOST = 5;

    public HealingNode(UUID id) {
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
        double healingAmount = pullDouble(2, context);
        masoCost = BASECOST * (float) healingAmount;
        super.execute(context);
        Object target = pullData(1, context);
        if(target instanceof LivingEntity targetEntity) {
            targetEntity.setHealth(targetEntity.getHealth() + (float) healingAmount);
        }
    }
}
