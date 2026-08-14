package com.github.sweetfish111.reincarnated.client.event;

import com.github.sweetfish111.reincarnated.client.ReincarnatedKeyMapping;
import com.github.sweetfish111.reincarnated.network.payload.CastMagicOnePayload;
import com.github.sweetfish111.reincarnated.network.payload.RequestCircuitPayload;
import com.github.sweetfish111.reincarnated.network.payload.StopCastPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = "reincarnated", value = Dist.CLIENT)
public class ClientModEvents {

    private static boolean wasKeyPressedLastTick = false;

    //インベントリが開かれたとき魔法編集ボタンをねじ込む
    @SubscribeEvent
    public static void onGuiInit(ScreenEvent.Init.Post event){
        if(event.getScreen() instanceof InventoryScreen){
            int screenWidth = event.getScreen().width;
            int screenHeight = event.getScreen().height;

            event.addListener(Button.builder(Component.literal("Magic Edit"),(button)->{
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
        KeyMapping magicKey1 = ReincarnatedKeyMapping.MAGIC_KEY_1.get();
        boolean isCurrentlyDown = magicKey1.isDown();

        //押された瞬間魔法詠唱開始
        if(isCurrentlyDown && !wasKeyPressedLastTick){
            CastMagicOnePayload payload = new CastMagicOnePayload();
            if(net.minecraft.client.Minecraft.getInstance().getConnection() != null){
                net.minecraft.client.Minecraft.getInstance().getConnection().send(payload);
            }
        }

        if(!isCurrentlyDown && wasKeyPressedLastTick){
            if(net.minecraft.client.Minecraft.getInstance().getConnection() != null){
                net.minecraft.client.Minecraft.getInstance().getConnection().send(new StopCastPayload());
            }
        }

        wasKeyPressedLastTick = isCurrentlyDown;
    }

    @SubscribeEvent
    public static void onMouseButtonPre(net.neoforged.neoforge.client.event.InputEvent.MouseButton.Pre event){
        if (event.getButton() == org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE
                && event.getAction() == org.lwjgl.glfw.GLFW.GLFW_PRESS) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.setScreenAndShow(new com.github.sweetfish111.reincarnated.client.screen.MagicWheelScreen());
                event.setCanceled(true); // デフォルトの中クリック（ブロックピック等）を抑止
            }
        }
    }
}
