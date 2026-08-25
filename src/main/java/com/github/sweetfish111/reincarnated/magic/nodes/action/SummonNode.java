package com.github.sweetfish111.reincarnated.magic.nodes.action;

import com.github.sweetfish111.reincarnated.config.BalanceConfig;
import com.github.sweetfish111.reincarnated.magic.casting.MasoInvestmentScaling;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.magic.summon.SummonBehavior;
import com.github.sweetfish111.reincarnated.magic.summon.SummonManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class SummonNode extends AbstractMagicNode {

    public SummonNode(UUID id) {
        super(id);
    }

    private float baseCost() {
        return BalanceConfig.SUMMON_BASE_COST.get().floatValue();
    }

    @Override
    public void execute(MagicContext context) {
        context.incrementAndCheck();

        Vec3 position = pullVector3(1, context);
        double investedMaso = pullMaso(2, context).amount();
        float availableMaso = (float) context.getMasoTank().getBalance();
        double behaviorRaw = pullDouble(3, context);

        if (position != null) {
            ServerLevel level = context.getLevel();
            UUID ownerId = context.getCaster().getCasterId();

            MasoInvestmentScaling.EffectResult effectResult =
                    MasoInvestmentScaling.computeEffect(baseCost(), (float) investedMaso, availableMaso);
            masoCost = effectResult.masoCost();

            // ★魔素チェック＆消費は召喚実行より先に行う（不足時は召喚が発生しないように）
            super.execute(context);

            int livingTicks = (int) Math.max(20, effectResult.effectAmount()); // 最低1秒

            SummonManager.createSummon(ownerId, position, livingTicks, SummonBehavior.fromId((int) behaviorRaw));

            if (level != null) {
                SummonManager.playSummonEffects(level, position);
            }
        }

        pushExecute(context);
    }

}
