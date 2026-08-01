package com.github.sweetfish111.reincarnated.event;

import com.github.sweetfish111.reincarnated.magic.ActiveMagicManager;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = "reincarnated")
public class ReincarnatedServerTick {
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post e){
        for (ServerLevel level : e.getServer().getAllLevels()) {
            ActiveMagicManager.onServerTick(level);
        }
    }
}
