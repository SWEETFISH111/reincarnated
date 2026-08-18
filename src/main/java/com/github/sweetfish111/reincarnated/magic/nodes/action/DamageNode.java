package com.github.sweetfish111.reincarnated.magic.nodes.action;

import com.github.sweetfish111.reincarnated.config.BalanceConfig;
import com.github.sweetfish111.reincarnated.magic.MasoAmount;
import com.github.sweetfish111.reincarnated.magic.casting.MasoInvestmentScaling;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.magic.nodes.MagicNode;
import com.github.sweetfish111.reincarnated.registry.ReincarnatedDamageTypes;
import com.github.sweetfish111.reincarnated.system.CausalityObserver;
import com.github.sweetfish111.reincarnated.system.ReincarnatedPlaySound;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;

import java.util.UUID;


public class DamageNode extends AbstractMagicNode implements MagicNode {
    float BASECOST = 2;
    private long lastDamageTick = Long.MIN_VALUE; // ★ノード単位の連射防止

    public DamageNode(UUID id) {
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
        long now = context.getLevel().getGameTime();
        int cooldown = BalanceConfig.DAMAGE_NODE_COOLDOWN_TICKS.get();
        if (now - lastDamageTick < cooldown) {
            // クールダウン中：Masoも消費せず、演出も出さず完全スキップ
            return;
        }

        double damageAmount = pullDouble(2, context);
        float availableMaso = context.getCaster().getMasoAmount();

        MasoInvestmentScaling.CostResult costResult =
                MasoInvestmentScaling.computeCost(BASECOST, (float) damageAmount, availableMaso);
        masoCost = costResult.cost();
        super.execute(context);

        if (costResult.isOvercharge() && context.getCaster().getCasterEntity() instanceof ServerPlayer player) {
            CausalityObserver.onOverCharge(player);
        }

        Object target = pullData(1, context);
        if (target instanceof Entity targetEntity) {
            DamageSource source;
            var damageType = context.getCaster().getCasterLevel().registryAccess()
                    .lookupOrThrow(Registries.DAMAGE_TYPE)
                    .getOrThrow(ReincarnatedDamageTypes.MAGIC_DAMAGE); // ★MAGIC→独自タイプに変更

            if (context.getCaster().getCasterEntity() instanceof ServerPlayer player) {
                source = new DamageSource(damageType, null, player, null);
            } else {
                source = new DamageSource(damageType, null, null, null);
            }
            targetEntity.hurtServer(context.getLevel(), source, costResult.grantedAmount());
            lastDamageTick = now; // ★実際にヒットした時だけクールダウン更新

            if (context.getCaster().getCasterEntity() instanceof ServerPlayer player) {
                ReincarnatedPlaySound.playHitSound(player.level(), player.getPosition(1.0f));
            }
        } else {
            ReincarnatedPlaySound.playMissSound(context.getCaster().getCasterLevel(), context.getCaster().getCasterPosition());
        }
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(portIndex, context);

        double damageAmount = pullDouble(2, context);
        float availableMaso = context.getCaster().getMasoAmount();

        MasoInvestmentScaling.CostResult costResult =
                MasoInvestmentScaling.computeCost(BASECOST, (float) damageAmount, availableMaso);

        return costResult.cost();
    }
}