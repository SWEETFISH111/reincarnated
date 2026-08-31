package com.github.sweetfish111.reincarnated.skill;

import net.minecraft.server.level.ServerPlayer;

public interface IFallEffectSkill {
    /** true を返すと落下ダメージを無効化する */
    boolean cancelFall(ServerPlayer player);
}
