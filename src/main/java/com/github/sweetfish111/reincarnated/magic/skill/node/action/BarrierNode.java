package com.github.sweetfish111.reincarnated.magic.skill.node.action;

import com.github.sweetfish111.reincarnated.init.ModAttachments;
import com.github.sweetfish111.reincarnated.magic.casting.MasoInvestmentScaling;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.magic.nodes.MagicNode;
import com.github.sweetfish111.reincarnated.player.PlayerMagicData;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BarrierNode extends AbstractMagicNode {
    float BASECOST = 3;

    public BarrierNode(UUID id) {
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
        context.incrementAndCheck();

        float desiredPoint = (float) pullDouble(1, context);
        if (desiredPoint <= 0) return;

        if (!(context.getCaster().getCasterEntity() instanceof ServerPlayer player)) return;
        PlayerMagicData magicData = player.getData(ModAttachments.PLAYER_MAGIC_DATA);

        float maxBarrierPoint = magicData.getMaxBarrierPoint();
        float currentBarrierPoint = magicData.getBarrierPoint();
        if (maxBarrierPoint <= currentBarrierPoint) return;

        float roomLeft = maxBarrierPoint - currentBarrierPoint;
        float requestedPoint = Math.min(desiredPoint, roomLeft);

        float currentMaso = context.getCaster().getMasoAmount();
        float cost = MasoInvestmentScaling.safeCost(BASECOST, requestedPoint);

        float grantedPoint;
        if (cost <= currentMaso) {
            grantedPoint = requestedPoint;
        } else {
            grantedPoint = Math.min(
                    requestedPoint,
                    MasoInvestmentScaling.maxAffordableSafeAmount(BASECOST, currentMaso)
            );
            cost = MasoInvestmentScaling.safeCost(BASECOST, grantedPoint);
        }

        if (grantedPoint > 0) {
            magicData.setBarrierPoint(currentBarrierPoint + grantedPoint);
            consumeMaso(cost, context.getMasoTank());
        }
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(portIndex, context);
        float desiredPoint = (float) pullDouble(1, context);

        if (desiredPoint <= 0) return 0.0;
        if (!(context.getCaster().getCasterEntity() instanceof ServerPlayer player)) return 0.0;

        PlayerMagicData magicData = player.getData(ModAttachments.PLAYER_MAGIC_DATA);
        float maxBarrierPoint = magicData.getMaxBarrierPoint();
        float currentBarrierPoint = magicData.getBarrierPoint();
        float roomLeft = maxBarrierPoint - currentBarrierPoint;
        float requestedPoint = Math.min(desiredPoint, roomLeft);

        return MasoInvestmentScaling.safeCost(BASECOST, requestedPoint);
    }
}