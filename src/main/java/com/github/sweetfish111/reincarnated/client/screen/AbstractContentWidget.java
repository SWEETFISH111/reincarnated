package com.github.sweetfish111.reincarnated.client.screen;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;

public abstract class AbstractContentWidget<T,V extends AbstractWidget> extends AbstractWidget{
    protected T currentValue;
    protected V contentWidget;
    DraggableNodeWidget parentNode;

    public AbstractContentWidget(int x, int y, int width, int height, Component message,DraggableNodeWidget parentNode) {
        super(x, y, width, height, message);
        this.parentNode = parentNode;
    }
    //getter
    public T getCurrentValue() {
        return currentValue;
    }
    public V getContentWidget(){
        return contentWidget;
    }
    //setter

    public void setX(int x){
        super.setX(x);
        contentWidget.setX(x);
    }
    public void setY(int y){
        super.setY(y);
        contentWidget.setY(y);
    }
    public void setHeight(int height){
        super.setHeight(height);
        contentWidget.setHeight(height);
    }
    public void setWidth(int width){
        super.setWidth(width);
        contentWidget.setWidth(width);
    }

    public boolean handleMouseClicked(int canvasX, int canvasY, int button, int buttonModifiers){
        MouseButtonInfo buttonInfo = new MouseButtonInfo(button, buttonModifiers);
        MouseButtonEvent canvasEvent = new MouseButtonEvent(canvasX, canvasY, buttonInfo);
        return contentWidget.mouseClicked(canvasEvent, false);
    }
}

