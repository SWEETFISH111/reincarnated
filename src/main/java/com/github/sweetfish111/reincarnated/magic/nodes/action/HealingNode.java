package com.github.sweetfish111.reincarnated.magic.nodes.action;

import com.github.sweetfish111.reincarnated.config.BalanceConfig;
import com.github.sweetfish111.reincarnated.magic.casting.MasoInvestmentScaling;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.system.ReincarnatedPlaySound;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

import java.util.UUID;


public class HealingNode extends AbstractMagicNode {

    public HealingNode(UUID id) {
        super(id);
    }

    private float baseCost() {
        return BalanceConfig.HEALING_BASE_COST.get().floatValue();
    }

    @Override
    public void execute(MagicContext context) {
        Object target = pullData(1, context);
        if(target instanceof LivingEntity targetEntity) {
            double investedMaso = pullDouble(2, context);
            float availableMaso = context.getCaster().getMasoAmount();

            MasoInvestmentScaling.EffectResult effectResult =
                    MasoInvestmentScaling.computeEffect(baseCost(), (float) investedMaso, availableMaso);
            masoCost = effectResult.masoCost();

            // ★魔素チェック＆消費はheal()適用より先に行う（不足時は回復が発生しないように）
            super.execute(context);

            targetEntity.heal(effectResult.effectAmount());
            ReincarnatedPlaySound.playHealSound(context.getCaster().getCasterLevel(), context.getCaster().getCasterPosition());
        }else{
            // ターゲットが取れなかった場合：Masoも消費せず、演出のみ
            ReincarnatedPlaySound.playMissSound(context.getCaster().getCasterLevel(), context.getCaster().getCasterPosition());
        }
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        double investedMaso = pullDouble(2, context);
        float availableMaso = context.getCaster().getMasoAmount();
        MasoInvestmentScaling.EffectResult effectResult =
                MasoInvestmentScaling.computeEffect(baseCost(), (float) investedMaso, availableMaso);
        return effectResult.masoCost();
    }
}
