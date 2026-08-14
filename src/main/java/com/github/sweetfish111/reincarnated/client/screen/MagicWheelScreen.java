package com.github.sweetfish111.reincarnated.client.screen;

import com.github.sweetfish111.reincarnated.network.payload.SelectMagicSlotPayload;
import com.github.sweetfish111.reincarnated.player.PlayerMagicData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class MagicWheelScreen extends Screen {
    private static final int SLOT_COUNT = PlayerMagicData.MAGIC_SLOT_COUNT;
    private int hoveredSlot = -1;

    public MagicWheelScreen() {
        super(Component.literal("Magic Wheel"));
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        double radius = 70;

        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        double distSq = dx * dx + dy * dy;

        // 中心付近（不感帯）ではどれも選択しない
        if (distSq > 20 * 20) {
            double angle = Math.toDegrees(Math.atan2(dy, dx)); // -180〜180、右方向が0度
            angle = (angle + 120 + 360) % 360; // 上方向(12時)を0度に補正
            hoveredSlot = (int) (angle / (360.0 / SLOT_COUNT));
        } else {
            hoveredSlot = -1;
        }

        for (int i = 0; i < SLOT_COUNT; i++) {
            double wedgeCenterAngle = Math.toRadians((i * (360.0 / SLOT_COUNT)) - 90);
            int labelX = centerX + (int) (Math.cos(wedgeCenterAngle) * radius);
            int labelY = centerY + (int) (Math.sin(wedgeCenterAngle) * radius);

            int color = (i == hoveredSlot) ? 0xFFFFFF55 : 0xFFFFFFFF;
            guiGraphicsExtractor.centeredText(
                    Minecraft.getInstance().font,
                    Component.literal(String.valueOf(i + 1)),
                    labelX, labelY, color
            );
        }

        // 中心の枠線（円の目安）
        guiGraphicsExtractor.outline(centerX - (int) radius, centerY - (int) radius, (int) radius * 2, (int) radius * 2, 0x88FFFFFF);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 2 /* GLFW_MOUSE_BUTTON_MIDDLE */) {
            if (hoveredSlot >= 0 && hoveredSlot < SLOT_COUNT
                    && Minecraft.getInstance().getConnection() != null) {
                Minecraft.getInstance().getConnection().send(new SelectMagicSlotPayload(hoveredSlot));
            }
            this.onClose();
            return true;
        }
        return super.mouseReleased(event);
    }
}