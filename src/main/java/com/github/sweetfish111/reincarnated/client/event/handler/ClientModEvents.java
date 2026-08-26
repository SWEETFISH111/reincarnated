package com.github.sweetfish111.reincarnated.client.event.handler;

import com.github.sweetfish111.reincarnated.blockentity.MagicCircleScreen;
import com.github.sweetfish111.reincarnated.blockentity.ReincarnatedMenus;
import com.github.sweetfish111.reincarnated.client.ReincarnatedKeyMapping;
import com.github.sweetfish111.reincarnated.network.payload.CastMagicOnePayload;
import com.github.sweetfish111.reincarnated.network.payload.RequestCircuitPayload;
import com.github.sweetfish111.reincarnated.network.payload.RequestStatusPayload;
import com.github.sweetfish111.reincarnated.network.payload.StopCastPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = "reincarnated", value = Dist.CLIENT)
public class ClientModEvents {

    private static boolean wasKeyPressedLastTick = false;

    // 現在生えているボタン（対象画面が閉じたらnullに戻す）
    private static Button magicEditButton;
    private static Button statusButton;

    private static boolean isTargetScreen(Screen screen) {
        return screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen;
    }

    //インベントリ／クリエイティブ画面が開かれたとき魔法編集ボタンをねじ込む
    @SubscribeEvent
    public static void onGuiInit(ScreenEvent.Init.Post event){
        magicEditButton = null;
        statusButton = null;

        if (!isTargetScreen(event.getScreen())) return;

        magicEditButton = Button.builder(Component.literal("Magic Edit"), button -> {
                    if (Minecraft.getInstance().getConnection() != null) {
                        Minecraft.getInstance().getConnection().send(new RequestCircuitPayload());
                    }
                })
                .bounds(0, 0, 60, 20) // 実座標はrepositionButtonsで毎フレーム上書き
                .build();

        statusButton = Button.builder(Component.literal("Status"), button -> {
                    if (Minecraft.getInstance().getConnection() != null) {
                        Minecraft.getInstance().getConnection().send(new RequestStatusPayload());
                    }
                })
                .bounds(0, 0, 60, 20)
                .build();

        event.addListener(magicEditButton);
        event.addListener(statusButton);
        repositionButtons(event.getScreen());
    }

    // レシピブック開閉・ウィンドウリサイズでleftPosが動いても追従させる
    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Pre event){
        if (magicEditButton == null || !isTargetScreen(event.getScreen())) return;
        repositionButtons(event.getScreen());
    }

    @SubscribeEvent
    public static void onScreenClosing(ScreenEvent.Closing event){
        if (isTargetScreen(event.getScreen())) {
            magicEditButton = null;
            statusButton = null;
        }
    }

    private static void repositionButtons(Screen screen){
        if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) return;

        int leftPos = containerScreen.getLeftPos();
        int topPos = containerScreen.getTopPos();
        int imageWidth = containerScreen.getImageWidth();
        int imageHeight = containerScreen.getImageHeight();

        int x = leftPos + imageWidth - 60 - 4 - 60 -4; // パネル右端にぴったり接する
        int y = topPos - 24;

        magicEditButton.setPosition(x, y);
        statusButton.setPosition(x + 64, y);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event){
        KeyMapping magicKey1 = ReincarnatedKeyMapping.MAGIC_KEY_1.get();
        boolean isCurrentlyDown = magicKey1.isDown();

        if(isCurrentlyDown && !wasKeyPressedLastTick){
            CastMagicOnePayload payload = new CastMagicOnePayload();
            if(Minecraft.getInstance().getConnection() != null){
                Minecraft.getInstance().getConnection().send(payload);
            }
        }

        if(!isCurrentlyDown && wasKeyPressedLastTick){
            if(Minecraft.getInstance().getConnection() != null){
                Minecraft.getInstance().getConnection().send(new StopCastPayload());
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
                event.setCanceled(true);
            }
        }
    }

}