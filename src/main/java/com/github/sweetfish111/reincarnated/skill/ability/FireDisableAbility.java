package com.github.sweetfish111.reincarnated.skill.ability;

import com.github.sweetfish111.reincarnated.skill.IDamageDisableSkill;
import com.github.sweetfish111.reincarnated.skill.ISkillAbility;
import com.github.sweetfish111.reincarnated.skill.SkillEffect;
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
