package com.github.sweetfish111.reincarnated.client.screen;

import com.github.sweetfish111.reincarnated.circuit.MagiculeNodeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

import java.util.UUID;

public class CompoundNodeWidget extends DraggableNodeWidget {
    private String customName;

    public CompoundNodeWidget(MagicEditorScreen parentScreen, UUID id, MagiculeNodeType type, int x, int y, int width){
        super(parentScreen, id, type, x, y, width);
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // ここで通常のノードとは違う「四角い箱型」の見た目を描画する！
        int bgColor = this.isHovered ? 0xFF334466 : 0xFF222233;
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + 40, bgColor); // 箱の高さやデザインを自由にカスタム
        guiGraphics.outline(this.getX(), this.getY(), this.width, 40, 0xFF66AACC);

        // タイトル文字の描画
        guiGraphics.centeredText(Minecraft.getInstance().font, this.customName, this.getX() + (this.width / 2), this.getY() + 16, 0xFFFFFF55);
    }
}
