package com.github.sweetfish111.reincarnated.system;

import com.github.sweetfish111.reincarnated.player.MasoEvolutionStage;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ReincarnatedPlaySound {
    public static void playEvolutionSound(Player player) {
        Level level = player.level();
        double x = player.getX();
        double y = player.getY() + player.getBbHeight() * 0.5;
        double z = player.getZ();

        level.playSound(null, x, y, z, SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 0.6F, 0.8F);
        level.playSound(null, x, y, z, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
        level.playSound(null, x, y, z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2F, 1.2F);
    }

    public static void playStageEvolutionSound(Player player, MasoEvolutionStage newStage) {
        Level level = player.level();
        double x = player.getX();
        double y = player.getY() + player.getBbHeight() * 0.5;
        double z = player.getZ();

        int tier = newStage.ordinal(); // STAGE0=0 〜 STAGE3=3
        float pitchFactor = Math.max(0.4f, 0.8f - tier * 0.12f);   // 段階が上がるほど低く重く
        float volumeFactor = Math.min(1.2f, 0.6f + tier * 0.15f);  // 段階が上がるほど大きく

        level.playSound(null, x, y, z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.6f * volumeFactor, pitchFactor);
        level.playSound(null, x, y, z, SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.35f * volumeFactor, pitchFactor + 0.3f);
        level.playSound(null, x, y, z, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f * volumeFactor, pitchFactor + 0.2f);
    }

    public static void playHitSound(Level level, Vec3 pos){
        level.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 0.8f, 1.0f);

        // 2. 魔力プラズマ衝撃音
        level.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.7f, 0.4f);

        // 3. 術式粉砕音（ガラス割れ音）
        level.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.9f, 0.5f);
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

    public static void playTeleportSound(Level level, Vec3 pos){
        level.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS,0.6f, 1.4f
        );
    }
}
