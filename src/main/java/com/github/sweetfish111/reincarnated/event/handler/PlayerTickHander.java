package com.github.sweetfish111.reincarnated.event.handler;

import com.github.sweetfish111.reincarnated.circuit.EditorTab;
import com.github.sweetfish111.reincarnated.magic.caster.PlayerCasterAdapter;
import com.github.sweetfish111.reincarnated.magic.casting.ActiveMagicManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;

@EventBusSubscriber(modid = "reincarnated")
public class PlayerTickHander {
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post e){
        if (!e.getEntity().level().isClientSide()) {
            MasoRegenHandler.onPlayerTick(e);
        }

        if(!e.getEntity().level().isClientSide()){
            Map<String, Object> data = null;
            ActiveMagicManager.executeEventTrigger(new PlayerCasterAdapter((ServerPlayer) e.getEntity()), EditorTab.SKILL, "on_tick", data);
        }

    }
}
