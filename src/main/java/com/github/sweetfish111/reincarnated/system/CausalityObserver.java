package com.github.sweetfish111.reincarnated.system;

import com.github.sweetfish111.reincarnated.circuit.EditorTab;
import com.github.sweetfish111.reincarnated.init.ModAttachments;
import com.github.sweetfish111.reincarnated.magic.casting.ActiveMagicManager;
import com.github.sweetfish111.reincarnated.player.PlayerMagicData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
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
            ActiveMagicManager.executeEventTrigger(player, EditorTab.SKILL, "on_xp_pickup", data);

            // プレイヤーの魂データを取り出して「貪欲者」としての理を進行させる
            PlayerMagicData magicData = player.getData(ModAttachments.PLAYER_MAGIC_DATA);
            magicData.addGreedScore(xpAmount * 0.5);
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

            // プレイヤーが直接トドメを刺した場合（生体討伐）
            PlayerMagicData data = player.getData(ModAttachments.PLAYER_MAGIC_DATA);
             if(data != null){
                 // 例：「捕食者」ルートのスコアを加算
                 data.addPredatorScore(1.0);
             }
        } else {
            // プレイヤーが関与していない死体（ワールドに転がった残骸など）を
            // プレイヤーが漁る・近づく等の判定のフック（後ほど拡張可能）
        }
    }
}

