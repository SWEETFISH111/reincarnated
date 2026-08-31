package com.github.sweetfish111.reincarnated.skill;

import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public interface IKillEffectSkill {
    void onKill(LivingEntity source, LivingEntity target);
}
