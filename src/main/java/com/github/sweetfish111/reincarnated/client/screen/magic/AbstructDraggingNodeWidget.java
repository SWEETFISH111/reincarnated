package com.github.sweetfish111.reincarnated.client.screen.magic;

import com.github.sweetfish111.reincarnated.circuit.PortDataType;
import com.github.sweetfish111.reincarnated.circuit.PortType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public abstract class AbstructDraggingNodeWidget extends AbstractWidget {

    protected final UUID id;
    protected final MagicEditorScreen parentScreen;

    protected boolean isDragging = false;
    protected double dragOffsetX = 0;
    protected double dragOffsetY = 0;
    protected double currentMouseX = 0;
    protected double currentMouseY = 0;

    protected final List<NodePort> inputPorts = new ArrayList<>();
    protected final List<NodePort> outputPorts = new ArrayList<>();

    protected AbstractContentWidget<?, ?> contentWidget;
    protected double contentOffsetX = 10;
    protected double contentOffsetY = 24;

    protected NodePort draggingPort = null;

    public AbstructDraggingNodeWidget(MagicEditorScreen parentScreen, UUID id, int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
        this.parentScreen = parentScreen;
        this.id = id;
    }

    //setter
    @Override
    public void setX(int x) {
        super.setX(x);
        if(this.contentWidget != null){
            this.contentWidget.setX(x);
        }
    }
    @Override
    public void setY(int y) {
        super.setY(y);
        if(this.contentWidget != null){
            this.contentWidget.setY(y);
        }
    }
    public void setDragging(boolean isDragging){this.isDragging = isDragging;}
    public void setDragOffset(double canvasX, double canvasY){
        this.dragOffsetX = canvasX - this.getX();
        this.dragOffsetY = canvasY - this.getY();
    }
    public void setContentOffset(double contentOffsetX, double contentOffsetY){
        this.contentOffsetX = contentOffsetX;
        this.contentOffsetY = contentOffsetY;
    }

    //getter
    public List<NodePort> getInputPorts(){return this.inputPorts;}
    public List<NodePort> getOutputPorts(){return this.outputPorts;}
    public MagicEditorScreen getParentScreen(){return this.parentScreen;}
    public UUID getId(){return this.id;}
    public AbstractContentWidget<?, ?> getContentWidget(){return this.contentWidget;};

    public void setupPorts(List<PortDataType> inputPorts, List<PortDataType> outputPorts){
        if(!this.inputPorts.isEmpty()){
            for(NodePort port : this.inputPorts){
                if(port != null) port.rightClicked(this, port);
            }
        }
        if(!this.outputPorts.isEmpty()){
            for(NodePort port : this.outputPorts){
                if(port != null) port.rightClicked(this, port);
            }
        }
        this.inputPorts.clear();
        this.outputPorts.clear();

        int maxPorts = Math.max(inputPorts.toArray().length, outputPorts.toArray().length);
        this.height = (maxPorts == 0) ? 40 : 20 + (maxPorts * 15) + 5;

        for(int i = 0; i < inputPorts.toArray().length; i++){
            this.inputPorts.add(new NodePort(this, PortType.INPUT, i, inputPorts.get(i)));
        }
        for(int i = 0; i < outputPorts.toArray().length; i++){
            this.outputPorts.add(new NodePort(this, PortType.OUTPUT, i, outputPorts.get(i)));
        }
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partianTick) {
        if(this.draggingPort != null){
            drawMagiculeWire(guiGraphicsExtractor, this.draggingPort.getX() + 3, this.draggingPort.getY() + 3, (int) this.currentMouseX, (int) this.currentMouseY);
        }
        int bgColor = this.isHovered ? 0xFFAAAAAA : 0xFF444444;
        bgColor = this.isFocused() ? 0xFFAAAAAA : bgColor;
        guiGraphicsExtractor.fill(getX(), getY(), getX() + width, getY() + height, bgColor);
        guiGraphicsExtractor.outline(getX(),  getY(), width, height, 0xFFFFFFFF);

        int textX = getX() + width / 2;
        int textY = getY() + 6;
        guiGraphicsExtractor.centeredText(Minecraft.getInstance().font, getMessage(), textX, textY, 0xFFFFFFFF);

        for(NodePort port : this.inputPorts){
            if(port != null) port.render(guiGraphicsExtractor, mouseX, mouseY);
        }
        for(NodePort port : this.outputPorts){
            if(port != null) port.render(guiGraphicsExtractor, mouseX, mouseY);
        }
        if(this.contentWidget != null){
            this.contentWidget.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partianTick);
        }
    }

    protected void drawMagiculeWire(GuiGraphicsExtractor guiGraphicsExtractor, int startX, int startY, int endX, int endY){
        int dx = endX - startX;
        int dy = endY - startY;

        int steps = Math.max(Math.abs(dx), Math.abs(dy)) / 4;

        if(steps == 0) return;

        float xInc = (float) dx / steps;
        float yInc = (float) dy / steps;
        float x = startX;
        float y = startY;

        for(int i = 0; i <= steps; i++){
            guiGraphicsExtractor.fill((int)x, (int)y, (int)x + 2, (int)y + 2, 0xFF00AAFF);
            x += xInc;
            y += yInc;
        }
    }

    public boolean handleCanvasClick(MouseButtonEvent sourceEvent, double canvasX, double canvasY){
        if(portClicked((int)canvasX, (int)canvasY, sourceEvent.button())){
            return true;
        }
        draggingPort = null;
        if(this.contentWidget != null){
            if(this.contentWidget.handleMouseClicked((int)canvasX, (int)canvasY, sourceEvent.button(), sourceEvent.modifiers())){
                this.contentWidget.setFocused(true);
                System.out.println("AbstructDraggingNodeWidget : contentsClick");
                return true;
            }
        }

        if(sourceEvent.button() == 0 && this.isMouseOver(canvasX, canvasY)){
            this.isDragging = true;
            this.dragOffsetX = canvasX - this.getX();
            this.dragOffsetY = canvasY - this.getY();
            return true;
        }else return this.isMouseOver(canvasX, canvasY);
    }

    public boolean portClicked(int canvasX, int canvasY, int button){
        for(NodePort port : this.inputPorts){
            if(port.isMouseOver(canvasX, canvasY)){
                if(button == 1){
                    port.rightClicked(this, port);
                    return true;
                }else if(button == 0){
                    this.draggingPort = port;
                    this.currentMouseX = canvasX;
                    this.currentMouseY = canvasY;
                    return true;
                }
            }
        }
        for(NodePort port : this.outputPorts){
            if(port.isMouseOver(canvasX,canvasY)){
                if(button == 1){
                    port.rightClicked(this, port);
                    return true;
                }else if(button == 0){
                    this.draggingPort = port;
                    this.currentMouseX = canvasX;
                    this.currentMouseY = canvasY;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean handleCanvasDragged(double canvasX, double canvasY, double dx, double dy){
        if(this.draggingPort != null){
            this.currentMouseX = (int) canvasX;
            this.currentMouseY = (int) canvasY;
            return true;
        }

        if(this.isDragging){
            this.setX((int)(canvasX - this.dragOffsetX));
            this.setY((int)(canvasY - this.dragOffsetY));
            if(contentWidget != null){
                contentWidget.setX(this.getX() + (int) contentOffsetX);
                contentWidget.setY(this.getY() + (int) contentOffsetY);
            }
            return true;
        }
        return false;
    }

    public boolean handleCanvasReleased(double cavasX, double canvasY, int button){
        this.isDragging = false;
        if(this.draggingPort != null){
            this.parentScreen.onWireDropped(this, this.draggingPort, cavasX, canvasY);
            this.draggingPort = null;
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.contentWidget != null && this.contentWidget.isFocused()) {
            return this.contentWidget.keyPressed(event);
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (this.contentWidget != null && this.contentWidget.isFocused()) {
            return this.contentWidget.charTyped(event);
        }
        return super.charTyped(event);
    }
}
