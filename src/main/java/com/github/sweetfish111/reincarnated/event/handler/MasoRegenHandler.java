package com.github.sweetfish111.reincarnated.event.handler;

import com.github.sweetfish111.reincarnated.init.ModAttachments;
import com.github.sweetfish111.reincarnated.network.payload.SyncMasoPayload;
import com.github.sweetfish111.reincarnated.player.PlayerMagicData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = "reincarnated")
public class MasoRegenHandler {
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event){
        if (event.getEntity() instanceof ServerPlayer player) {

            PlayerMagicData magicData = player.getData(ModAttachments.PLAYER_MAGIC_DATA);

            float current = magicData.currentMaso;
            float max = magicData.getMaxMaso();


            if (current < max) {
                float regenPerTick = magicData.getMasoRegenRate() / 20.0f;

                magicData.currentMaso += regenPerTick;
                magicData.totalRegeneratedMaso += regenPerTick;
                PacketDistributor.sendToPlayer(player, new SyncMasoPayload(magicData.getMaxMaso(), magicData.currentMaso));
            }
        }
    }
}
