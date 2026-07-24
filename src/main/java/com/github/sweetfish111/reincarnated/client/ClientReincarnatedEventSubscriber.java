package com.github.sweetfish111.reincarnated.client;

import com.github.sweetfish111.reincarnated.entity.reincarnatedEntityTypes;
import com.github.sweetfish111.reincarnated.reincarnated;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = reincarnated.MODID, value = Dist.CLIENT)
public class ClientReincarnatedEventSubscriber {

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event){
        event.registerEntityRenderer(
                reincarnatedEntityTypes.CUSTOM_MAGIC_PROJECTILE.get(),
                context -> new ThrownItemRenderer<>(context)
        );
    }
}
