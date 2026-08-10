package com.github.sweetfish111.reincarnated.system;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;

public class ReincarnatedPlaySound {
    public static void playEvolutionSound(Player player) {
        player.playSound(
                SoundEvents.END_PORTAL_SPAWN,
                0.6F, // 音量
                0.8F  // ピッチを少し下げて重低音感を強調
        );

        // 2. システムの覚醒・権能授与（シャキーン！）
        player.playSound(
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                1.0F,
                1.0F
        );

        // 3. 結晶化・定着の余韻（キラリーン）
        player.playSound(
                SoundEvents.AMETHYST_BLOCK_CHIME,
                1.2F,
                1.2F  // 高めのピッチで神聖さを出す
        );
    }
}
