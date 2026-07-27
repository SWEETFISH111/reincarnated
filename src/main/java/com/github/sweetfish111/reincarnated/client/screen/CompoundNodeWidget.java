package com.github.sweetfish111.reincarnated.client.screen;

import com.github.sweetfish111.reincarnated.circuit.MagiculeNodeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import java.util.UUID;

public class CompoundNodeWidget extends AbstructDraggingNodeWidget {
    private final String id = "compound";
    private final MagiculeNodeType type = MagiculeNodeType.COMPOUND;
    private String customName;
    private int castCost = 1;

    public CompoundNodeWidget(MagicEditorScreen parentScreen, UUID id, int x, int y, int width){
        super(parentScreen, id, x, y, width, 40, Component.literal("compound"));
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

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        //TODO nanikore
    }
}
