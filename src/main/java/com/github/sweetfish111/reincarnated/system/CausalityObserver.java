package com.github.sweetfish111.reincarnated.system;

import com.github.sweetfish111.reincarnated.circuit.EditorTab;
import com.github.sweetfish111.reincarnated.event.PlayerUniqueSkillAcquiredEvent;
import com.github.sweetfish111.reincarnated.init.ModAttachments;
import com.github.sweetfish111.reincarnated.magic.caster.PlayerCasterAdapter;
import com.github.sweetfish111.reincarnated.magic.casting.ActiveMagicManager;
import com.github.sweetfish111.reincarnated.player.PlayerMagicData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
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

            Map<String, Object> data = Map.of("xp_amount", (double) xpAmount);
            ActiveMagicManager.executeEventTrigger(new PlayerCasterAdapter(player), EditorTab.SKILL, "on_xp_pickup", data);

            // プレイヤーの魂データを取り出して「貪欲者」としての理を進行させる
            PlayerMagicData magicData = player.getData(ModAttachments.PLAYER_MAGIC_DATA);
            magicData.addGreedyScore(xpAmount * 0.5);
            magicData.totalConsumedMaso += xpAmount;
        }
    }

    /**
     * 2. 生物の死（キル）を観測 —— 「捕食者」や「飢餓者」のスコア判定
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            LivingEntity target = event.getEntity();
            double killScore = 20;

            Map<String, Object> data = Map.of("kill", killScore);
            ActiveMagicManager.executeEventTrigger(new PlayerCasterAdapter(player), EditorTab.SKILL, "on_kill", data);

            PlayerMagicData magicData = player.getData(ModAttachments.PLAYER_MAGIC_DATA);
            // 例：「捕食者」ルートのスコアを加算
            magicData.addPredatorScore(0.2);
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

                PlayerMagicData magicData = player.getData(ModAttachments.PLAYER_MAGIC_DATA);
                magicData.addScavengerScore(1);
            }
        }

    }
    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Pre event){
        if(event.getEntity() instanceof ServerPlayer player){
            Map<String, Object> data = Map.of("damageAmount", event.getOriginalDamage());
            ActiveMagicManager.executeEventTrigger(new PlayerCasterAdapter(player), EditorTab.SKILL, "on_damage", data);
        }

    }
}

