package com.github.sweetfish111.reincarnated.client.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.github.sweetfish111.reincarnated.circuit.ContentWidgetType;
import com.github.sweetfish111.reincarnated.circuit.MagiculeNodeType;
import java.util.Optional;
import net.minecraft.Optionull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;

public class DraggableNodeWidget extends AbstractWidget {
    private final UUID id;
    private final MagiculeNodeType type;
    private final MagicEditorScreen parentScreen;

    private boolean isDragging = false;
    private double dragOffsetX = 0;
    private double dragOffsetY = 0;

    private final List<NodePort> inputPorts = new ArrayList<>();
    private final List<NodePort> outputPorts = new ArrayList<>();
    private AbstractContentWidget contentWidget;

    private NodePort draggingPort = null;
    private int currentMouseX = 0;
    private int currentMouseY = 0;

    //コンストラクタ
    public DraggableNodeWidget(MagicEditorScreen screen,UUID id, MagiculeNodeType type, int x, int y, int width){
        super(x, y, width, 0, Component.literal(type.displayName));
        this.parentScreen = screen;
        this.type = type;
        this.id = id;
        if(this.type.getContent() != ContentWidgetType.NONE){
            if(this.type.getContent() == ContentWidgetType.NUMBER_INPUT){
                this.contentWidget = new NumberInputContentWidget(
                        Minecraft.getInstance().font,
                        x + 10, y + 24, 60, 12,
                        Component.literal("数値入力"),
                        this
                );
            }
        }

        int maxPorts = Math.max(type.inputs.length, type.outputs.length);
        this.height = 20 + (maxPorts * 15) + 5;

        for(int i = 0; i < type.inputs.length; i++){
            this.inputPorts.add(new NodePort(this, NodePort.Type.INPUT, i, type.inputs[i]));
        }
        for(int i = 0; i < type.outputs.length; i++){
            this.outputPorts.add(new NodePort(this, NodePort.Type.OUTPUT, i, type.outputs[i]));
        }

    }

    //ゲッター
    public UUID getId(){return this.id;}
    public MagiculeNodeType getType(){return this.type;}
    public List<NodePort> getInputPorts(){return this.inputPorts;}
    public List<NodePort> getOutputPorts(){return this.outputPorts;}
    public MagicEditorScreen getParentScreen(){return this.parentScreen;}
    public AbstractContentWidget getContentWidget(){return contentWidget;}

    //セッター
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

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
        if(this.draggingPort != null){
            drawMagiculeWire(guiGraphicsExtractor, this.draggingPort.getX() + 3, this.draggingPort.getY() + 3, this.currentMouseX, this.currentMouseY);
        }

        int bgColor = this.isHovered ? 0xFF666666 : 0xFF444444;
        guiGraphicsExtractor.fill(getX(), getY(), getX() + width, getY() + height, bgColor);
        guiGraphicsExtractor.outline(getX(), getY(), width, height, 0xFFFFFFFF);

        int textX = getX() + width / 2;
        int textY = getY() + 6;
        guiGraphicsExtractor.centeredText(Minecraft.getInstance().font, getMessage(), textX, textY, 0xFFFFFFFF);

        for(NodePort port : this.inputPorts) port.render(guiGraphicsExtractor, mouseX, mouseY);
        for(NodePort port : this.outputPorts) port.render(guiGraphicsExtractor, mouseX, mouseY);

        if(this.contentWidget != null){
            this.contentWidget.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTick);
        }
    }

    private void drawMagiculeWire(GuiGraphicsExtractor guiGraphicsExtractor, int startX, int startY, int endX, int endY){
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

    public boolean handleCanvasClick(MouseButtonEvent sourceEvent, double canvasX, double canvasY, int button){
        if(this.contentWidget != null){
            MouseButtonInfo mouseInfo = new MouseButtonInfo(0,0);
            MouseButtonEvent mouseButtonEvent = new MouseButtonEvent(canvasX, canvasY, mouseInfo);
            if(this.contentWidget.mouseClicked(mouseButtonEvent, false)){
                this.contentWidget.setFocused(true);
                return true;
            }else{
                this.contentWidget.setFocused(false);
            }
        }
        for(NodePort port : this.inputPorts){
            if(port.isMouseOver(canvasX, canvasY)){
                if(button == 1){
                    this.parentScreen.onPortRightClicked(this, port);
                    return true;
                }else if(button == 0){
                    this.draggingPort = port;

                    this.currentMouseX = (int)canvasX;
                    this.currentMouseY = (int)canvasY;
                    return true;
                }
            }
        }
        for(NodePort port : this.outputPorts){
            if(port.isMouseOver(canvasX, canvasY)){
                if(button == 1){
                    this.parentScreen.onPortRightClicked(this, port);
                    return true;
                }else if(button == 0){
                    this.draggingPort = port;
                    this.currentMouseX = (int)canvasX;
                    this.currentMouseY = (int)canvasY;
                    return true;
                }
            }
        }
        if(button == 0 && this.isMouseOver(canvasX, canvasY)){
            this.isDragging = true;
            this.dragOffsetX = canvasX - this.getX();
            this.dragOffsetY = canvasY - this.getY();
            return true;
        }

        return false;
    }

    public boolean handleCanvasDragged(double canvasX, double canvasY, double dx, double dy) {
        if(this.draggingPort != null){
            this.currentMouseX = (int) canvasX;
            this.currentMouseY = (int) canvasY;
            return true;
        }

        if(this.isDragging){
            this.setX((int)(canvasX - this.dragOffsetX));
            this.setY((int)(canvasY - this.dragOffsetY));
            if(contentWidget != null){
                contentWidget.setX(this.getX() + 10);
                contentWidget.setY(this.getY() + 24);
            }
            return true;
        }
        return false;
    }

    public boolean handleCanvasReleased(double canvasX, double casnvasY, int button) {
        this.isDragging = false;

        if(this.draggingPort != null){
            this.parentScreen.onWireDropped(this, this.draggingPort, canvasX, casnvasY);
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

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
