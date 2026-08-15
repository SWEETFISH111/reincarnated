package com.github.sweetfish111.reincarnated.event.handler;

import com.github.sweetfish111.reincarnated.magic.casting.ActiveMagicManager;
import com.github.sweetfish111.reincarnated.magic.casting.CastingManager;
import com.github.sweetfish111.reincarnated.magic.casting.DelayCastingManager;
import com.github.sweetfish111.reincarnated.magic.casting.TimerCastingManager;
import com.github.sweetfish111.reincarnated.magic.summon.SummonManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = "reincarnated")
public class PlayerLogoutHandler {
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ActiveMagicManager.unregisterAllForPlayer(serverPlayer.getUUID());
            SummonManager.unregisterAllForOwner(serverPlayer.getUUID());
            CastingManager.cancelCasting(serverPlayer.getUUID());
            DelayCastingManager.cancelTasksForCaster(serverPlayer.getUUID());
            TimerCastingManager.cancelTasksForCaster(serverPlayer.getUUID());
        }
    }
}
