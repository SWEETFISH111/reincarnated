package com.github.sweetfish111.reincarnated.skill;

import com.github.sweetfish111.reincarnated.config.BalanceConfig;
import com.github.sweetfish111.reincarnated.init.ReincarnatedAttachments;
import com.github.sweetfish111.reincarnated.player.PhysicalData;
import com.github.sweetfish111.reincarnated.player.SoulData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class SkillMasteryManager {

    /** PHYSICAL側スキルの発動を1回記録し、閾値到達なら移行する */
    public static void recordUsageAndCheckMigration(ServerPlayer player, SkillEffect effect) {
        if (effect.getDomain() != SkillDomain.PHYSICAL) return; // SOUL由来は対象外

        PhysicalData physical = player.getData(ReincarnatedAttachments.PHYSICAL_DATA);
        if (!physical.hasOwnedSkillEffect(effect)) return; // 既に移行済みなら何もしない

        int progress = physical.incrementMastery(effect);
        if (progress >= BalanceConfig.MASTERY_THRESHOLD.getAsInt()) {
            migrateToSoul(player, effect);
        }
    }

    private static void migrateToSoul(ServerPlayer player, SkillEffect effect) {
        PhysicalData physical = player.getData(ReincarnatedAttachments.PHYSICAL_DATA);
        SoulData soul = player.getData(ReincarnatedAttachments.SOUL_DATA);

        physical.removeOwnedSkillEffect(effect);
        physical.clearMastery(effect);

        soul.addOwnedSkillEffect(effect);
        soul.activateSkillEffect(effect); // 自動的に有効化

        player.sendSystemMessage(Component.translatable(
                "message.reincarnated.voice_of_world.skill_became_soulbound",
                player.getName().getString(), effect.getSerializedName()));
    }
}