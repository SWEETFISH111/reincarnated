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
    public static float currentMaso = 0;
    public static float maxMaso = 20;
    public static float currentBarrire = 0;
    public static float maxBarrier= 20;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        // 描画するテキスト
        String masoText = String.format("魔素: %.1f / %.1f", currentMaso, maxMaso);

        // 画面の左上（またはお好みの位置）にテキストを描画
        int masoX = (int)(event.getGuiGraphics().guiWidth() * (85f / 100f));
        int masoY = (int)(event.getGuiGraphics().guiHeight() * (95f / 100f));
        int masoColor = (currentMaso >= maxMaso) ? 0xFFFFFFFF : 0xFF55FFFF;
        event.getGuiGraphics().centeredText(
                Minecraft.getInstance().font,
                masoText,
                masoX,
                masoY,
                masoColor
        );

        String barrierText = String.format("バリア: %.1f / %.1f", currentBarrire, maxBarrier);

        int barrierX = (int)(event.getGuiGraphics().guiWidth() * (15f / 100f));
        int barrierY = (int)(event.getGuiGraphics().guiHeight() * (95f / 100f));
        int barrierColor = 0xFFFFFFFF;
        event.getGuiGraphics().centeredText(
                Minecraft.getInstance().font,
                barrierText,
                barrierX,
                barrierY,
                barrierColor
        );
    }
}
