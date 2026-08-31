package com.github.sweetfish111.reincarnated.skill;

import com.github.sweetfish111.reincarnated.init.ReincarnatedAttachments;
import com.github.sweetfish111.reincarnated.player.AbstractSkillHolder;
import net.minecraft.server.level.ServerPlayer;

import java.util.EnumSet;
import java.util.Set;

public class SkillHolderResolver {

    public static AbstractSkillHolder resolve(ServerPlayer player, SkillDomain domain) {
        return switch (domain) {
            case PHYSICAL -> player.getData(ReincarnatedAttachments.PHYSICAL_DATA);
            case SOUL -> player.getData(ReincarnatedAttachments.SOUL_DATA);
        };
    }

    public static Set<SkillEffect> getAllActiveSkillEffects(ServerPlayer player) {
        Set<SkillEffect> result = EnumSet.noneOf(SkillEffect.class);
        result.addAll(player.getData(ReincarnatedAttachments.PHYSICAL_DATA).getActiveSkillEffects());
        result.addAll(player.getData(ReincarnatedAttachments.SOUL_DATA).getActiveSkillEffects());
        return result;
    }
}