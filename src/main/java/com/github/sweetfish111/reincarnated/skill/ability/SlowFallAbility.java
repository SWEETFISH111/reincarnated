package com.github.sweetfish111.reincarnated.skill.ability;

import com.github.sweetfish111.reincarnated.skill.IFallEffectSkill;
import com.github.sweetfish111.reincarnated.skill.ISkillAbility;
import com.github.sweetfish111.reincarnated.skill.SkillEffect;
import net.minecraft.server.level.ServerPlayer;

public class SlowFallAbility implements ISkillAbility, IFallEffectSkill {
    @Override
    public boolean cancelFall(ServerPlayer player) {
        return true; // SlowFallは無条件に落下ダメージを無効化する
    }

    @Override
    public SkillEffect getAssociatedAbility() {
        return SkillEffect.SLOW_FALL;
    }
}
