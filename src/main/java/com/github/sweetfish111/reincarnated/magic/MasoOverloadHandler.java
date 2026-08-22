package com.github.sweetfish111.reincarnated.magic;

import com.github.sweetfish111.reincarnated.config.BalanceConfig;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.registry.ReincarnatedDamageTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import static com.github.sweetfish111.reincarnated.config.BalanceConfig.*;
import static java.lang.Math.exp;
import static java.lang.Math.max;

public class MasoOverloadHandler {
    public static OverloadResult attemptOverload(MagicContext context, double overflowAmount){
        if (overflowAmount <= 0) {
            return new OverloadResult(false, 1.0);
        }
        double chance = max(
                OVERLOAD_MIN_CHANCE.getAsDouble(),
                OVERLOAD_BASE_CHANCE.getAsDouble() * exp( -overflowAmount / OVERLOAD_CHANCE_DECAY_SCALE.getAsDouble() )
        );
        if(Math.random() >= chance)return new OverloadResult(false, 1.0);

        double selfDamage = OVERLOAD_DAMAGE_PER_OVERFLOW.getAsDouble() * overflowAmount;
        if(context.getCaster().getCasterEntity() instanceof LivingEntity entity){
            var damageType = context.getCaster().getCasterLevel().registryAccess()
                    .lookupOrThrow(Registries.DAMAGE_TYPE)
                    .getOrThrow(ReincarnatedDamageTypes.MAGIC_DAMAGE);
            DamageSource source = new DamageSource(damageType, null, entity, null);
            entity.hurtServer(context.getCaster().getCasterLevel(), source, (float) selfDamage);
        }

        double bonusPerOverflow = OVERLOAD_BONUS_PER_OVERFLOW.get();
        double exponent = OVERLOAD_BONUS_EXPONENT.get();
        double bonusMultiplier = 1.0 + bonusPerOverflow * Math.pow(overflowAmount, exponent);

        return new OverloadResult(true, bonusMultiplier);

    }
}
