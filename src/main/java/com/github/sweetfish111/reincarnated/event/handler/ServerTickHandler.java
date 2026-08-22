package com.github.sweetfish111.reincarnated.event.handler;

import com.github.sweetfish111.reincarnated.magic.casting.ActiveMagicManager;
import com.github.sweetfish111.reincarnated.magic.casting.CastingManager;
import com.github.sweetfish111.reincarnated.magic.casting.DelayCastingManager;
import com.github.sweetfish111.reincarnated.magic.casting.TimerCastingManager;
import com.github.sweetfish111.reincarnated.magic.summon.SummonManager;
import com.github.sweetfish111.reincarnated.world.LandMasoDensityData;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = "reincarnated")
public class ServerTickHandler {
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post e){
        for (ServerLevel level : e.getServer().getAllLevels()) {
            ActiveMagicManager.onServerTick(level);
            SummonManager.onServerTick(level);
            if(level.getOverworldClockTime() % 24000 == 0){
                LandMasoDensityData.get(level).refillAllToMax(level);
            }
        }
        CastingManager.onServerTick();
        DelayCastingManager.onServerTick();
        TimerCastingManager.onServerTick();

    }
}
