package com.github.sweetfish111.reincarnated.client.screen.magic;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class CycleContentWidget<T extends ICycleButtonValue> extends AbstractContentWidget<T, CycleButton<T>>{

    public CycleContentWidget(int x, int y, int width, int height, Component message, DraggableNodeWidget parentNode, T[] values, T defaultValue){
        super(x, y, width, height, message, parentNode);
        currentValue = defaultValue;
        contentWidget = CycleButton.<T>builder(v -> v.getDisplayName(), currentValue)
                .withValues(values)
                .create(x, y, width, height, message, (button, value) ->{
                    currentValue = value;
                    parentNode.getParentScreen().getThisLayerManager().getWorkCircuit().setNodeParam(this.parentNode.getId(), "value", currentValue);
                });
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialtick) {
        contentWidget.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialtick);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        contentWidget.updateWidgetNarration(narrationElementOutput);
    }
}
