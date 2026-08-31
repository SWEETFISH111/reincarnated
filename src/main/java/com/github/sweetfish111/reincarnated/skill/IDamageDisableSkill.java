package com.github.sweetfish111.reincarnated.skill;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

public interface IDamageDisableSkill {
    /** true を返すとこのダメージソースを完全無効化する */
    boolean isDisable(ServerPlayer player, DamageSource source);
}