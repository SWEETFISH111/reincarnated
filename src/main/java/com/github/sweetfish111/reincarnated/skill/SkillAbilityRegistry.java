package com.github.sweetfish111.reincarnated.skill;

import com.github.sweetfish111.reincarnated.init.ReincarnatedAttachments;
import com.github.sweetfish111.reincarnated.player.PhysicalData;
import com.github.sweetfish111.reincarnated.player.PlayerMagicData;
import com.github.sweetfish111.reincarnated.reincarnated;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = reincarnated.MODID)
public class SkillAbilityRegistry {
    private static final Map<SkillEffect, ISkillAbility> REGISTRY = new EnumMap<>(SkillEffect.class);

    static {
        List<ISkillAbility> abilities = List.of(
                new SlowFallAbility(),
                new FireDisableAbility()
        );

        for (ISkillAbility ability : abilities) {
            SkillEffect key = ability.getAssociatedAbility();
            if (REGISTRY.putIfAbsent(key, ability) != null) {
                throw new IllegalStateException("SkillEffect " + key + " に複数のISkillAbilityが登録されています");
            }
        }
    }

    public static ISkillAbility get(SkillEffect effect) {
        return REGISTRY.get(effect);
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        for (SkillEffect effect : SkillHolderResolver.getAllActiveSkillEffects(player)) {
            ISkillAbility ability = REGISTRY.get(effect);
            if (ability instanceof IFallEffectSkill fallSkill && fallSkill.cancelFall(player)) {
                event.setCanceled(true);
                return;
            }
        }
    }
}
