package com.github.sweetfish111.reincarnated.client.screen;

import com.github.sweetfish111.reincarnated.circuit.ContentWidgetType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class NumberInputContentWidget extends AbstractContentWidget implements INodeNumberInput{
    private final EditBox editBox;
    private final DraggableNodeWidget parentNode;
    private boolean isUpdating = false;
    private double currentValue;


    public NumberInputContentWidget(Font font, int x, int y, int width, int height, Component message, DraggableNodeWidget parentNode){
        super(x, y, width, height, message);
        editBox = new EditBox(
                font,
                x, y, width, height,
                message
        );
        this.parentNode = parentNode;

        this.isUpdating = true;
        if(this.parentNode.getParentScreen() != null && this.parentNode.getParentScreen().getCircuit() != null){
            double savedValue = this.parentNode.getParentScreen().getCircuit().getNodeParam(this.parentNode.getId(), "value", 0.0);
            this.currentValue = savedValue;
            this.editBox.setValue(String.valueOf(savedValue));
        }
        this.isUpdating = false;
        this.editBox.setResponder(val ->{
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
    protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor guiGraphicsExtractor, int X, int Y, float partialTickTime) {
        this.editBox.setX(this.getX());
        this.editBox.setY(this.getY());
        this.editBox.extractWidgetRenderState(guiGraphicsExtractor, X, Y, partialTickTime);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        super.mouseClicked(event, doubleClick);
        return this.editBox.mouseClicked(event, doubleClick);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        //未定
    }

    @Override
    public double getCurrentvalue() {
        return currentValue;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return editBox.charTyped(event);
    }

    @Override
    public boolean handleCharTyped(CharacterEvent codePoint) {
        return this.charTyped(codePoint);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return editBox.keyPressed(event);
    }

    @Override
    public boolean handleKeyPressed(KeyEvent scanCode) {
        return this.keyPressed(scanCode);
    }

    //getter & setter
    @Override
    public EditBox getEditBox(){return this.editBox;}

    @Override
    public void setX(int x) {
        super.setX(x);
        if(this.editBox != null){
            this.editBox.setX(x);
        }
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        if(this.editBox != null){
            this.editBox.setY(y);
        }
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        this.editBox.setFocused(focused);
    }
}
