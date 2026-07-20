package com.github.sweetfish111.reincarnated.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

import java.util.HashMap;
import java.util.UUID;

public class SwitchContentWidget extends AbstractContentWidget<Boolean,Button> {

    public SwitchContentWidget(int x, int y, int width, int height, Component message, DraggableNodeWidget parentNode) {
        super(x, y, width, height, message, parentNode);

        // 1. デフォルト値を設定
        this.currentValue = false;

        // 2. セーブデータ（回路データ）があれば前回のパラメータを復元
        if (this.parentNode != null && this.parentNode.getParentScreen() != null && this.parentNode.getParentScreen().getCircuit() != null) {
            Object saved = this.parentNode.getParentScreen().getCircuit().getNodeParam(this.parentNode.getId(), "value", false);
            if (saved instanceof Boolean b) {
                this.currentValue = b;
            } else if (saved instanceof Number n) { // ByteTag(1/0)やDouble(0.0)対策
                this.currentValue = n.doubleValue() != 0.0;
            }
        }

        // 3. 復元された currentValue に基づいてボタンを生成
        Button.OnPress onPress = (val -> this.changeCurrentValue());
        Component savedMessage = currentValue ? Component.literal("有効").withColor(TextColor.GREEN) : Component.literal("無効").withColor(TextColor.RED);
        this.contentWidget = Button.builder(savedMessage, onPress).build();
        this.contentWidget.setX(x);
        this.contentWidget.setY(y);
        this.contentWidget.setWidth(width);
        this.contentWidget.setHeight(height);
    }

    public void changeCurrentValue(){
        if(currentValue){
            currentValue = false;
            contentWidget.setMessage(Component.literal("無効").withColor(TextColor.RED));
        }else {
            currentValue = true;
            contentWidget.setMessage(Component.literal("有効").withColor(TextColor.GREEN));
        }
        parentNode.getParentScreen().getCircuit().setNodeParam(this.parentNode.getId(), "value", currentValue);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTickTime) {
        this.contentWidget.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTickTime);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        //今度実装します。
    }
}
