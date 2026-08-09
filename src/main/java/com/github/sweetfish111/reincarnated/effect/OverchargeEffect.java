package com.github.sweetfish111.reincarnated.effect;

import com.github.sweetfish111.reincarnated.init.ModAttachments;
import com.github.sweetfish111.reincarnated.system.CausalityObserver;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class OverchargeEffect extends MobEffect {

    public OverchargeEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplification) {

        return tickCount % 20 == 0;
    }

    @Override
    public boolean applyEffectTick(ServerLevel serverLevel, LivingEntity mob, int amplification) {
        if (!serverLevel.isClientSide() && mob instanceof Player player) {

            // ① リジェネ処理（例: 1秒ごとに (1 + 段階) HPを回復）
            float healAmount = 1.0f * (amplification + 1);
            if (player.getHealth() < player.getMaxHealth()) {
                player.heal(healAmount);
            }

            CausalityObserver.addHoarderScore((ServerPlayer) player);
        }
        return true;
    }
}