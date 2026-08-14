package com.github.sweetfish111.reincarnated.event.handler;

import com.github.sweetfish111.reincarnated.effect.ReincarnatedEffects;
import com.github.sweetfish111.reincarnated.init.ModAttachments;
import com.github.sweetfish111.reincarnated.network.payload.SyncMasoPayload;
import com.github.sweetfish111.reincarnated.player.PlayerMagicData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class MasoRegenHandler {
    public static void onPlayerTick(PlayerTickEvent event){
        if (event.getEntity() instanceof ServerPlayer player) {

            PlayerMagicData magicData = player.getData(ModAttachments.PLAYER_MAGIC_DATA);

            float current = magicData.getCurrentMaso();
            float max = magicData.getMaxMaso();


            if (current < max) {
                float regenPerTick = magicData.getMasoRegenRate() / 20.0f;

                magicData.addMasoAmount(regenPerTick);
                magicData.addTotalRegeneratedMaso(regenPerTick);
                if(magicData.getCurrentMaso() > max){
                    magicData.setCurrentMaso(max);
                }
            }

            if(current > max){
                float attenuationPerTick = 0.003f;

                if (!player.hasEffect(ReincarnatedEffects.OVERCHARGE)) {
                    player.addEffect(new MobEffectInstance(
                            ReincarnatedEffects.OVERCHARGE, // NeoForge 1.21+ / 26.2 の MobEffect 取得方法
                            40, // 2秒間維持
                            0,  // amplifier (レベル1)
                            false, // ambient (環境エフェクトか)
                            true   // visible (パーティクルを表示するか)
                    ));
                }

                magicData.addMasoAmount(-attenuationPerTick);
                if(magicData.getCurrentMaso() < max){
                    magicData.setCurrentMaso(max);
                }
            }

            PacketDistributor.sendToPlayer(player, new SyncMasoPayload(magicData.getMaxMaso(), magicData.getCurrentMaso(), magicData.getMaxBarrierPoint(), magicData.getBarrierPoint()));
        }
    }
}
