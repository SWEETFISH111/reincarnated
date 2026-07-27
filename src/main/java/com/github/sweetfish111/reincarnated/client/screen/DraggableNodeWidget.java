package com.github.sweetfish111.reincarnated.client.screen;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.github.sweetfish111.reincarnated.circuit.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

import javax.sound.sampled.Port;

public class DraggableNodeWidget extends AbstructDraggingNodeWidget {
    private final MagiculeNodeType type;

    //コンストラクタ
    public DraggableNodeWidget(MagicEditorScreen parentScreen,UUID id, int x, int y, int width, MagiculeNodeType type){
        super(parentScreen, id, x, y, width, 0, Component.literal(type.displayName));
        this.type = type;
        boolean savedValue = false;
        Object param = this.parentScreen.getThisLayerManager().getWorkCircuit().getNodeParam(this.id, "value", false);
        if (param instanceof Boolean b) {
            savedValue = b;
        }

        if(this.type.getContent() != ContentWidgetType.NONE){
            switch (this.type.getContent()){
                case ContentWidgetType.NUMBER_INPUT:
                    this.contentWidget = new NumberInputContentWidget(
                            Minecraft.getInstance().font,
                            x + 10, y + 24, 60, 12,
                            Component.literal("数値入力"),
                            this
                    );
                    break;
                case MODE_SELECT:
                case ContentWidgetType.SWITCH:
                    this.contentWidget = new SwitchContentWidget(
                            x + 50, y + 20, 25, 12,
                            savedValue ? Component.literal("有効").withColor(TextColor.GREEN) : Component.literal("無効").withColor(TextColor.RED),
                            this
                    );
                    break;
                default:
                    break;
            }
        }
        setupPorts(Arrays.asList(this.type.inputs), Arrays.asList(this.type.outputs));
    }

    //ゲッター
    public MagiculeNodeType getType(){return this.type;}

    @Override
    public boolean handleCanvasClick(MouseButtonEvent sourceEvent, double canvasX, double canvasY){
        if(this.contentWidget != null){
            if(this.contentWidget.handleMouseClicked((int)canvasX, (int)canvasY, sourceEvent.button(), sourceEvent.modifiers())){
                this.contentWidget.setFocused(true);
                if(this.type.getContent() == ContentWidgetType.MODE_SELECT){
                    SwitchContentWidget switchWidget = (SwitchContentWidget)contentWidget;
                    this.draggingPort = null;
                    boolean currentState = switchWidget.getCurrentValue();

                    this.parentScreen.getThisLayerManager().getWorkCircuit().setNodeParam(this.getId(), "value", currentState);

                    this.parentScreen.getThisLayerManager().getWorkCircuit().removeWiresByNode(this.getId());
                    List<PortDataType> targetPorts = (currentState) ? Arrays.asList(type.anotherInputs) : Arrays.asList(type.inputs);
                    setupPorts(targetPorts, Arrays.asList(type.outputs));
                }

                return true;
            }else{
                this.contentWidget.setFocused(false);
            }
        }

        return super.handleCanvasClick(sourceEvent, canvasX, canvasY);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partianTick) {
        super.extractWidgetRenderState(guiGraphicsExtractor, mouseX, mouseY, partianTick);
        Object currentValue = this.parentScreen.getThisLayerManager().getWorkCircuit().getNodeParam(this.getId(), "value", false);
        boolean currentState = false;
        if(currentValue instanceof Boolean b){
            currentState = b.booleanValue();
        }
        List<PortDataType> targetPorts = (currentState) ? Arrays.asList(type.anotherInputs) : Arrays.asList(type.inputs);
        setupPorts(targetPorts, Arrays.asList(type.outputs));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        //todo nanikore
    }
}
