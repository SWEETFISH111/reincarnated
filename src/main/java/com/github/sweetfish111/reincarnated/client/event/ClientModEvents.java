package com.github.sweetfish111.reincarnated.client.event;

import com.github.sweetfish111.reincarnated.client.ReincarnatedKeyMapping;
import com.github.sweetfish111.reincarnated.client.screen.MagicEditorScreen;
import com.github.sweetfish111.reincarnated.network.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = "reincarnated", value = Dist.CLIENT)
public class ClientModEvents {

    //インベントリが開かれたとき魔法編集ボタンをねじ込む
    @SubscribeEvent
    public static void onGuiInit(ScreenEvent.Init.Post event){
        if(event.getScreen() instanceof InventoryScreen){
            int screenWidth = event.getScreen().width;
            int screenHeight = event.getScreen().height;

            event.addListener(Button.builder(Component.literal("魔法編集"),(button)->{
                if(Minecraft.getInstance().getConnection() != null){
                    Minecraft.getInstance().getConnection().send(new RequestCircuitPayload());
                }
            })
                    .bounds(screenWidth / 2 + 120, screenHeight / 2 -25, 40, 20)
                    .build());
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event){
        while(ReincarnatedKeyMapping.MAPPING.get().consumeClick()){
            CastMagicOnePayload payload = new CastMagicOnePayload();
            if(net.minecraft.client.Minecraft.getInstance().getConnection() != null){
                net.minecraft.client.Minecraft.getInstance().getConnection().send(payload);
            }
        }
    }
}
