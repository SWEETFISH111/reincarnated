package com.github.sweetfish111.reincarnated.skill;

import com.github.sweetfish111.reincarnated.init.ReincarnatedAttachments;
import com.github.sweetfish111.reincarnated.player.PlayerMagicData;
import com.github.sweetfish111.reincarnated.reincarnated;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;

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
