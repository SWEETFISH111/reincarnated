package com.github.sweetfish111.reincarnated.system;

import com.github.sweetfish111.reincarnated.circuit.EditorTab;
import com.github.sweetfish111.reincarnated.init.ReincarnatedAttachments;
import com.github.sweetfish111.reincarnated.magic.caster.PlayerCasterAdapter;
import com.github.sweetfish111.reincarnated.magic.casting.ActiveMagicManager;
import com.github.sweetfish111.reincarnated.player.PhysicalData;
import com.github.sweetfish111.reincarnated.player.PlayerMagicData;
import com.github.sweetfish111.reincarnated.reincarnated;
import com.github.sweetfish111.reincarnated.skill.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;

import java.util.Map;

@EventBusSubscriber(modid = "reincarnated")
public class CausalityObserver {
    /**
     * 1. 経験値（魂の破片）を獲得した瞬間を観測
     */
    @SubscribeEvent
    public static void onXpPickup(PlayerXpEvent.PickupXp event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            int xpAmount = event.getOrb().getValue();

            Map<String, Object> data = Map.of("xp_amount", xpAmount);
            ActiveMagicManager.executeEventTrigger(new PlayerCasterAdapter(player), EditorTab.SKILL, "on_xp_pickup", data);

            // プレイヤーの魂データを取り出して「貪欲者」としての理を進行させる
            PlayerMagicData magicData = player.getData(ReincarnatedAttachments.PLAYER_MAGIC_DATA);
            double score = (double) xpAmount * 0.2;
            reincarnated.LOGGER.info("CausalityObserver" + String.valueOf(score));
            magicData.addGreedyScore((double) xpAmount * 0.06, player);
        }
    }

    /**
     * 2. 生物の死（キル）を観測 —— 「捕食者」や「飢餓者」のスコア判定
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            LivingEntity target = event.getEntity();

            for (SkillEffect effect : SkillHolderResolver.getAllActiveSkillEffects(player)) {
                ISkillAbility ability = SkillAbilityRegistry.get(effect);
                if (ability instanceof IKillEffectSkill killEffect) {
                    killEffect.onKill(player, target);
                }
            }

            PlayerMagicData magicData = player.getData(ReincarnatedAttachments.PLAYER_MAGIC_DATA);
            double killScore = target.getMaxHealth();

            Map<String, Object> data = Map.of("killScore", killScore);
            ActiveMagicManager.executeEventTrigger(new PlayerCasterAdapter(player), EditorTab.SKILL, "on_kill", data);

            // 例：「捕食者」ルートのスコアを加算
            magicData.addPredatorScore(0.2, player);
        }
    }

    @SubscribeEvent
    public static void onEatSomthing(LivingEntityUseItemEvent.Finish event){
        if (event.getEntity() instanceof ServerPlayer player) {
            ItemStack stack = event.getItem();
            FoodProperties food = stack.get(DataComponents.FOOD);
            if (food != null) {
                Map<String, Object>data = Map.of("satietyLevel", food.saturation());
                ActiveMagicManager.executeEventTrigger(new PlayerCasterAdapter(player), EditorTab.SKILL, "on_eat", data);

                PlayerMagicData magicData = player.getData(ReincarnatedAttachments.PLAYER_MAGIC_DATA);
                magicData.addScavengerScore(0.5, player);
            }
        }

    }

    public static void onOverCharge(ServerPlayer player){
        player.getData(ReincarnatedAttachments.PLAYER_MAGIC_DATA).addhoarderScore(0.2, player);
        Map<String, Object>data = null;
        ActiveMagicManager.executeEventTrigger(new PlayerCasterAdapter(player), EditorTab.SKILL, "on_overcharge", data);
    }

    @SubscribeEvent
    public static void onAttackStronger(AttackEntityEvent event){
        if(event.getEntity() instanceof ServerPlayer player){
            if(event.getTarget() instanceof LivingEntity){
                LivingEntity target = (LivingEntity) event.getTarget();
                double atackerAtk = player.getAttribute(Attributes.ATTACK_DAMAGE) != null
                        ? player.getAttributeValue(Attributes.ATTACK_DAMAGE) : 0.0;
                double targetAtk = target.getAttribute(Attributes.ATTACK_DAMAGE) != null
                        ? target.getAttributeValue(Attributes.ATTACK_DAMAGE) : 0.0;
                if(targetAtk > atackerAtk){
                    Map<String, Object> data = Map.of("power_gap", targetAtk - atackerAtk);
                    ActiveMagicManager.executeEventTrigger(new PlayerCasterAdapter(player), EditorTab.SKILL, "on_attack_stronger", data);

                    PlayerMagicData magicData = player.getData(ReincarnatedAttachments.PLAYER_MAGIC_DATA);
                    magicData.addUsurperScore(0.5, player);
                }
            }
        }
    }
}

