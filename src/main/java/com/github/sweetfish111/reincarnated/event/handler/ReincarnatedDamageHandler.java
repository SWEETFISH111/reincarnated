package com.github.sweetfish111.reincarnated.event.handler;

import com.github.sweetfish111.reincarnated.circuit.EditorTab;
import com.github.sweetfish111.reincarnated.init.ReincarnatedAttachments;
import com.github.sweetfish111.reincarnated.magic.caster.PlayerCasterAdapter;
import com.github.sweetfish111.reincarnated.magic.casting.ActiveMagicManager;
import com.github.sweetfish111.reincarnated.player.PhysicalData;
import com.github.sweetfish111.reincarnated.player.PlayerMagicData;
import com.github.sweetfish111.reincarnated.skill.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Map;

@EventBusSubscriber(modid = "reincarnated")
public class ReincarnatedDamageHandler {

    private static final float BARRIER_DAMAGE_REDUCTION = 0.35f;

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // スキル処理（炎無効）
            for (SkillEffect effect : SkillHolderResolver.getAllActiveSkillEffects(player)) {
                ISkillAbility ability = SkillAbilityRegistry.get(effect);
                if (ability instanceof IDamageDisableSkill immunity
                        && immunity.isDisable(player, event.getSource())) {
                    event.setNewDamage(0);
                    SkillMasteryManager.recordUsageAndCheckMigration(player, effect);
                    return;
                }
            }

            //バリア処理
            PlayerMagicData magicData = player.getData(ReincarnatedAttachments.PLAYER_MAGIC_DATA);
            Map<String, Object> data = Map.of("damageAmount", event.getOriginalDamage());
            ActiveMagicManager.executeEventTrigger(new PlayerCasterAdapter(player), EditorTab.SKILL, "on_damage", data);

            if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;

            float barrierPoint = magicData.getBarrierPoint();
            if (barrierPoint <= 0) return;

            float rawDamage = event.getOriginalDamage();

            // ① まず一律半減（バリア展開中は常時発動する下駄）
            float effectiveDamage = rawDamage * (1.0f - BARRIER_DAMAGE_REDUCTION);

            // ② 半減後のダメージをbarrierPointから定数引き算
            boolean barrierBroke; // ★追加：分岐結果を後で記録処理に渡すため保持
            if (barrierPoint >= effectiveDamage) {
                barrierPoint -= effectiveDamage;
                magicData.setBarrierPoint(barrierPoint);
                event.setNewDamage(0);
                playBarrierHitEffects(player, true);
                barrierBroke = false;
            } else {
                float finalDamage = effectiveDamage - barrierPoint;
                magicData.setBarrierPoint(0);
                event.setNewDamage(finalDamage);
                playBarrierHitEffects(player, false);
                barrierBroke = true;
            }

            long currentTick = player.level().getGameTime();
            magicData.recordBarrierHit(rawDamage, barrierBroke, currentTick);
        }
    }

    private static void playBarrierHitEffects(Player player, boolean absorbed) {
        if (!(player.level() instanceof ServerLevel level)) return;

        // プレイヤーの中心（胸の高さ）の座標を取得
        double x = player.getX();
        double y = player.getY() + player.getBbHeight() * 0.5;
        double z = player.getZ();

        if (absorbed) {
            // ==========================================
            // 🛡️ ケース1：バリアで完全ガード（弾いた時）
            // ==========================================

            // 1. 効果音：盾で防いだ重厚な音 ＆ エンチャントのシャキーン音
            level.playSound(
                    null,
                    x, y, z,
                    SoundEvents.SHIELD_BLOCK,
                    SoundSource.PLAYERS,
                    1.0f, 1.2f // 少しピッチを高めにして「魔力で弾いた」感を出す
            );
            level.playSound(
                    null,
                    x, y, z,
                    SoundEvents.ENCHANTMENT_TABLE_USE,
                    SoundSource.PLAYERS,
                    0.5f, 1.8f
            );

            // 2. パーティクル：術者の周囲を囲むように「魔素文字/閃光」を展開（球面スポーン）
            // count = 0 の技法で中心から外側へ拡散させる動きをつける
            level.sendParticles(
                    ParticleTypes.ENCHANT, // 魔素文字
                    x, y, z,
                    25,                    // 粒子数
                    0.8, 0.8, 0.8,         // 散らばり範囲（バリアの球体をイメージ）
                    0.2                    // 速度
            );

            level.sendParticles(
                    ParticleTypes.WAX_OFF, // 青白い閃光スパーク
                    x, y, z,
                    10,
                    0.5, 0.5, 0.5,
                    0.05
            );

        } else {
            // ==========================================
            // 💥 ケース2：バリア破壊・貫通（割れた時）
            // ==========================================

            // 1. 効果音：ガラス・結界が粉々になる不吉な破壊音
            level.playSound(
                    null,
                    x, y, z,
                    SoundEvents.GLASS_BREAK,
                    SoundSource.PLAYERS,
                    1.2f, 0.8f // 低めのピッチで「重厚な結界の砕散」を表現
            );
            level.playSound(
                    null,
                    x, y, z,
                    SoundEvents.GENERIC_EXPLODE.value(),
                    SoundSource.PLAYERS,
                    0.4f, 2.0f
            );

            // 2. パーティクル：結界が破片となって周囲へ弾け飛ぶ演出
            level.sendParticles(
                    ParticleTypes.CRIT,    // 衝撃波スパーク
                    x, y, z,
                    40,
                    0.3, 0.3, 0.3,
                    0.5 // 高速で飛び散らせる
            );

            level.sendParticles(
                    ParticleTypes.POOF,    // 崩壊の煙
                    x, y, z,
                    15,
                    0.4, 0.4, 0.4,
                    0.02
            );
        }
    }
}
