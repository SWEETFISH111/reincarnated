package com.github.sweetfish111.reincarnated.skill.ability;

import com.github.sweetfish111.reincarnated.config.BalanceConfig;
import com.github.sweetfish111.reincarnated.datamap.InnateSkills;
import com.github.sweetfish111.reincarnated.init.ReincarnatedDataMaps;
import com.github.sweetfish111.reincarnated.player.AbstractSkillHolder;
import com.github.sweetfish111.reincarnated.skill.IKillEffectSkill;
import com.github.sweetfish111.reincarnated.skill.ISkillAbility;
import com.github.sweetfish111.reincarnated.skill.SkillEffect;
import com.github.sweetfish111.reincarnated.skill.SkillHolderResolver;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public class SoulEaterAbility implements ISkillAbility, IKillEffectSkill {
    @Override
    public SkillEffect getAssociatedAbility() {
        return SkillEffect.SOUL_EATER;
    }

    @Override
    public void onKill(LivingEntity source, LivingEntity target) {
        Holder<EntityType<?>> targetType = target.getType().builtInRegistryHolder();
        InnateSkills innateSkills = targetType.getData(ReincarnatedDataMaps.INNATE_SKILLS);
        if (innateSkills == null) return; // target側がnullでのアーリーリターン

        if (source instanceof ServerPlayer player) {
            for (SkillEffect skill : innateSkills.skills()) {
                AbstractSkillHolder holder = SkillHolderResolver.resolve(player, skill.getDomain());
                if (!holder.hasOwnedSkillEffect(skill) && Math.ceil(Math.random() * 100) <= BalanceConfig.SOULEATER_PROBABILITY.getAsDouble()) {
                    holder.addOwnedSkillEffect(skill);
                    player.sendSystemMessage(Component.translatable(
                            "message.reincarnated.voice_of_world.get_new_skills",
                            player.getName().getString(), skill.getSerializedName()));
                }
            }
        }
    }
}
