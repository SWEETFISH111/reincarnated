package com.github.sweetfish111.reincarnated.system;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

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

    public static void playHitSound(Level level, Vec3 pos){
        level.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.2f, 1.0f);

        // 2. 魔力プラズマ衝撃音
        level.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0f, 0.4f);

        // 3. 術式粉砕音（ガラス割れ音）
        level.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.4f, 0.5f);
    }

    public static void playMissSound(Level level, Vec3 pos){
        level.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.5f, 1.6f);

        // 2. 魔素の蒸発・散逸音
        level.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.3f, 1.8f);
    }

    public static void playHealSound(Level level, Vec3 pos){
        level.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.PLAYERS, 0.8f, 1.2f);

        // 2. 権能解放・完了の残響（ビーコンの高音で修復完了を演出）
        level.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.6f, 1.8f);
    }

    public static void playToggleOnSound(Level level, Vec3 pos){
        level.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.LEVER_CLICK, SoundSource.PLAYERS,1.4f,0.6f
        );
        level.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS,1.6f,0.8f
        );
    }

    public static void playToggleOffSound(Level level, Vec3 pos){
        level.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.LEVER_CLICK, SoundSource.PLAYERS,0.8f,0.6f
        );
        level.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS,0.7f,0.5f
        );
    }
}
