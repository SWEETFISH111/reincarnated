package com.github.sweetfish111.reincarnated.magic.nodes.action;

import com.github.sweetfish111.reincarnated.magic.casting.MasoInvestmentScaling;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.magic.summon.SummonBehavior;
import com.github.sweetfish111.reincarnated.magic.summon.SummonManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class SummonNode extends AbstractMagicNode {
    float BASECOST = 1.2f;

    public SummonNode(UUID id) {
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
        context.incrementAndCheck();

        Vec3 position = pullVector3(1, context);
        double livingSeconds = pullDouble(2, context);
        double behaviorRaw = pullDouble(3, context);

        if (position != null) {
            ServerLevel level = context.getLevel();
            UUID ownerId = context.getCaster().getCasterId();
            int livingTicks = (int) Math.max(20, livingSeconds * 20); // 最低1秒

            float availableMaso = context.getCaster().getMasoAmount();

            MasoInvestmentScaling.CostResult costResult =
                    MasoInvestmentScaling.computeCost(BASECOST, (float) livingTicks, availableMaso);
            masoCost = costResult.cost();

            SummonManager.createSummon(ownerId, position, (int)costResult.grantedAmount(), SummonBehavior.fromId((int) behaviorRaw));

            super.execute(context);
            if (level != null) {
                SummonManager.playSummonEffects(level, position);
            }
        }

        pushExecute(context);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(portIndex, context);
        double livingSeconds = pullDouble(2, context);
        int livingTicks = (int) Math.max(20, livingSeconds * 20); // 最低1秒
        float availableMaso = context.getCaster().getMasoAmount();
        MasoInvestmentScaling.CostResult costResult =
                MasoInvestmentScaling.computeCost(BASECOST, (float) livingTicks, availableMaso);
        return costResult.cost();
    }
}