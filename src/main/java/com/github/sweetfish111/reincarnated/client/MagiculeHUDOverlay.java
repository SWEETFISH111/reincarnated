package com.github.sweetfish111.reincarnated.client;

import com.github.sweetfish111.reincarnated.player.PlayerMagicData;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = "reincarnated", value = Dist.CLIENT)
public class MagiculeHUDOverlay {
    public static PlayerMagicData magicData;
    public static float current = 0;
    public static float max = 20;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        // 描画するテキスト
        String text = String.format("魔素: %.1f / %.1f", current, max);

        // 画面の左上（またはお好みの位置）にテキストを描画
        int x = (int)(event.getGuiGraphics().guiWidth() * (60f / 100f));
        int y = (int)(event.getGuiGraphics().guiHeight() * (80f / 100f));
        int color = (current >= max) ? 0xFFFFFFFF : 0xFF55FFFF;
        event.getGuiGraphics().centeredText(
                Minecraft.getInstance().font,
                text,
                x,
                y,
                color
        );
    }
}
