package com.github.sweetfish111.reincarnated.skill;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;

public class FireDisableAbility implements ISkillAbility, IDamageDisableSkill {
    @Override
    public boolean isDisable(ServerPlayer player, DamageSource source) {
        return source.is(DamageTypeTags.IS_FIRE);
    }

    @Override
    public SkillEffect getAssociatedAbility() {
        return SkillEffect.FLAME_RESISTANCE;
    }
}
