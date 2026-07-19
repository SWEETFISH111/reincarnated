package com.github.sweetfish111.reincarnated.client.screen;

import com.github.sweetfish111.reincarnated.circuit.PortDataType;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import javax.sound.sampled.Port;

public class NodePort {
    public enum Type{INPUT, OUTPUT}

    private final PortDataType dataType;

    private final DraggableNodeWidget parentNode;
    private final Type type;
    private final int index;
    private final int size = 6;

    public NodePort(DraggableNodeWidget parentNode, Type type, int index, PortDataType dataType){
        this.parentNode = parentNode;
        this.type = type;
        this.index = index;
        this.dataType = dataType;
    }


    //ゲッター
    public PortDataType getDataType(){return this.dataType;}
    public int getX(){
        if(this.type == Type.INPUT){
            return parentNode.getX() - (size / 2);
        }else{
            return parentNode.getX() + parentNode.getWidth() - (size / 2);
        }
    }
    public int getY(){
        int totalPorts = (this.type == type.INPUT) ? parentNode.getInputPorts().size() : parentNode.getOutputPorts().size();
        int spacing = parentNode.getHeight() / (totalPorts + 1);
        return parentNode.getY() + (spacing * (this.index + 1)) - (size / 2);
    }
    public Type getType(){
        return this.type;
    }
    public int getIndex(){
        return this.index;
    }

    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY){
        int color = isMouseOver(mouseX, mouseY) ? 0xFF55FFFF : this.dataType.color;

        guiGraphicsExtractor.fill(getX(), getY(), getX() + size, getY() + size, color);
        guiGraphicsExtractor.outline(getX(), getY(), size, size, 0xFFFFFFFF);
    }

    public boolean isMouseOver(double mouseX, double mouseY){
        return mouseX >= getX() && mouseX <= getX() + size &&
                mouseY >= getY() && mouseY <= getY() + size;
    }

    public void rightClicked(DraggableNodeWidget node, NodePort port){
        parentNode.getParentScreen().getCircuit().removeWiresByPort(node.getId(), port.getType(), port.getIndex());
    }
}
