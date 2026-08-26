package com.github.sweetfish111.reincarnated.magic.nodes.action;

import com.github.sweetfish111.reincarnated.config.BalanceConfig;
import com.github.sweetfish111.reincarnated.magic.casting.MasoInvestmentScaling;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.system.CausalityObserver;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class ExplosionNode extends AbstractMagicNode {

    public ExplosionNode(UUID id){
        super(id);
    }

    private float baseCost() {
        return BalanceConfig.EXPLOSION_BASE_COST.get().floatValue();
    }

    @Override
    public void execute(MagicContext context) {
        double investedMaso = pullMaso(2, context).amount();
        float availableMaso = (float) (context.getMasoTank().getBalance() + context.getCaster().getMasoAmount());

        MasoInvestmentScaling.EffectResult effectResult =
                MasoInvestmentScaling.computeEffect(baseCost(), (float) investedMaso, availableMaso);
        masoCost = effectResult.masoCost();
        super.execute(context);

        if (effectResult.isOvercharge() && context.getCaster().getCasterEntity() instanceof ServerPlayer player) {
            CausalityObserver.onOverCharge(player); // 既存のhoarderスコア加算＋on_overchargeトリガーに相乗り
        }

        BlockPos targetPos = BlockPos.containing(pullVector3(1, context));
        Level.ExplosionInteraction interaction = Level.ExplosionInteraction.NONE;
        if (context.getCircuit() != null) {
            Object switchBoolean = context.getCircuit().getNodeParam(this.id, "value", false);
            if (switchBoolean instanceof Boolean b && b) {
                interaction = Level.ExplosionInteraction.TNT;
            }
        }
        if(context.getCaster().getCasterLevel() instanceof ServerLevel serverLevel){
            float explosionPower = effectResult.effectAmount();
            Entity sourceEntity = context.getCaster().getCasterEntity();
            serverLevel.explode(
                    sourceEntity,
                    targetPos.getX(), targetPos.getY(), targetPos.getZ(),
                    explosionPower,
                    interaction
            );
        }
        pushExecute(context);
    }

}
