package com.github.sweetfish111.reincarnated.event.handler;

import com.github.sweetfish111.reincarnated.event.ActiveMagicHandler;
import com.github.sweetfish111.reincarnated.magic.ActiveMagicManager;
import com.github.sweetfish111.reincarnated.magic.casting.CastingManager;
import com.github.sweetfish111.reincarnated.magic.casting.DelayCastingManager;
import com.github.sweetfish111.reincarnated.magic.casting.DelayCastingTask;
import com.github.sweetfish111.reincarnated.magic.casting.TimerCastingManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = "reincarnated")
public class ServerTickHandler {
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post e){
        ActiveMagicHandler.onServerTick(e.getServer());
        CastingManager.onServerTick();
        DelayCastingManager.onServerTick();
        TimerCastingManager.onServerTick();
    }
}
