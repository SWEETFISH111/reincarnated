package com.github.sweetfish111.reincarnated.client.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class NumberInputContentWidget extends AbstractContentWidget<Double, EditBox>{
    private boolean isUpdating = false;


    public NumberInputContentWidget(Font font, int x, int y, int width, int height, Component message, DraggableNodeWidget parentNode){
        super(x, y, width, height, message, parentNode);
        contentWidget = new EditBox(
                font,
                x, y, width, height,
                message
        );

        this.isUpdating = true;
        if(this.parentNode.getParentScreen() != null && this.parentNode.getParentScreen().getCircuit() != null){
            Object savedValue = this.parentNode.getParentScreen().getCircuit().getNodeParam(this.parentNode.getId(), "value", 0.0);
            if(savedValue instanceof Double doubleValue){
                this.currentValue = doubleValue;
            }
            this.contentWidget.setValue(String.valueOf(currentValue));
        }
        this.isUpdating = false;
        this.contentWidget.setResponder(val ->{
            if(this.isUpdating)return;
            if(val.isEmpty() || val.equals("-") || val.equals(".")) return;
            try{
                this.currentValue = Double.parseDouble(val);
                this.parentNode.getParentScreen().getCircuit().setNodeParam(this.parentNode.getId(), "value", this.currentValue);
            }catch(NumberFormatException e){

            }
        });
    }

    @Override
    protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor guiGraphicsExtractor, int x, int y, float partialTickTime) {
        this.contentWidget.extractWidgetRenderState(guiGraphicsExtractor, x, y, partialTickTime);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        //未定
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return contentWidget.charTyped(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return contentWidget.keyPressed(event);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        this.contentWidget.setFocused(focused);
    }
}
