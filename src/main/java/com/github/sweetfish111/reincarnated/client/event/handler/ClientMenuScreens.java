package com.github.sweetfish111.reincarnated.client.event.handler;

import com.github.sweetfish111.reincarnated.blockentity.MagicCircleScreen;
import com.github.sweetfish111.reincarnated.blockentity.ReincarnatedMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = "reincarnated", value = Dist.CLIENT)
public class ClientMenuScreens {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        System.out.println("=== RegisterMenuScreensEvent 発火 ===");
        event.register(ReincarnatedMenus.MAGIC_CIRCLE_MENU.get(), MagicCircleScreen::new);
        System.out.println("=== Screen登録完了 ===");
    }
}