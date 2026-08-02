package com.github.sweetfish111.reincarnated.event.handler;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = "reincarnated")
public class PlayerTickHander {
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post e){
        if (!e.getEntity().level().isClientSide()) {
            MasoRegenHandler.onPlayerTick(e);
        }
    }
}
