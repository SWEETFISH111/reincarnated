package com.github.sweetfish111.reincarnated.magic.nodes.action;

import com.github.sweetfish111.reincarnated.magic.casting.MasoInvestmentScaling;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.system.ReincarnatedPlaySound;
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
        float availableMaso = context.getCaster().getMasoAmount();

        MasoInvestmentScaling.CostResult costResult =
                MasoInvestmentScaling.computeCost(BASECOST, (float) healingAmount, availableMaso);
        masoCost = costResult.cost();
        Object target = pullData(1, context);
        if(target instanceof LivingEntity targetEntity) {;
            targetEntity.heal(costResult.grantedAmount());
            super.execute(context);
            ReincarnatedPlaySound.playHealSound(context.getLevel(), context.getCaster().getCasterPosition());
        }else{
            ReincarnatedPlaySound.playMissSound(context.getLevel(), context.getCaster().getCasterPosition());
        }
    }
}
