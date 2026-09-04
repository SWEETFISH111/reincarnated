package com.github.sweetfish111.reincarnated.client.screen.magic;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.circuit.MagiculeNodeType;
import com.github.sweetfish111.reincarnated.circuit.PortDataType;
import com.github.sweetfish111.reincarnated.magic.skill.SkillAccessLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class CompoundNodeWidget extends AbstructDraggingNodeWidget {
    private String customName;
    private int castCost = 1;
    private MagiculeCircuit.CompoundNodeData nodeData;
    private List<MagiculeCircuit.NodeData> innerInputProxys = new ArrayList<>();
    private List<MagiculeCircuit.NodeData> innerOutputProxys = new ArrayList<>();

    public CompoundNodeWidget(MagicEditorScreen parentScreen, UUID id, int x, int y, int width, String customName){
        super(parentScreen, id, x, y, width, 0, Component.literal("compound"));
        this.customName = customName;

        for(MagiculeCircuit.CompoundNodeData data : parentScreen.getWorkCircuit().getCompoundNodes()){
            if(data.id.equals(this.id)){
                nodeData = data;
            }
        }

        List<PortDataType> existInputs = new ArrayList<>();
        List<PortDataType> existOutputs = new ArrayList<>();
        for(MagiculeCircuit.NodeData node : nodeData.getCompoundCircuit().getNodes()){
            if(node.type == MagiculeNodeType.INPUT_PROXY){
                innerInputProxys.add(node);
                existInputs.add(PortDataType.ANY);
            }
            if(node.type == MagiculeNodeType.OUTPUT_PROXY){
                innerOutputProxys.add(node);
                existOutputs.add(PortDataType.ANY);
            }
        }
        super.setupPorts(existInputs, existOutputs);
    }

    public MagiculeCircuit.CompoundNodeData getLinkedData(){
        return this.nodeData;
    }
    public String getCustomName(){return this.customName;}

    public void openContents(){
        SkillAccessLevel access = this.getLinkedData().getAccessLevelFor(parentScreen.getMagicData());
        if (!access.canModify()) {
            parentScreen.triggerError(Component.translatable("message.reincarnated.compound_accessDenied"));
            return;
        }
        MagiculeCircuit parentCircuit = parentScreen.getWorkCircuit();
        MagiculeCircuit contentCircuit = nodeData.getCompoundCircuit();

        parentCircuit.getNodes().addAll(contentCircuit.getNodes());
        parentCircuit.getCompoundNodes().addAll(contentCircuit.getCompoundNodes());
        parentCircuit.getWires().addAll(contentCircuit.getWires());
        parentCircuit.getNodeParameters().putAll(contentCircuit.getNodeParameters());
        parentCircuit.removeNodeAndWires(this.id);

        parentScreen.rebuildNodeWidgets();
    }

    public void renderLockedOverLay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick){
        SkillAccessLevel access = this.getLinkedData().getAccessLevelFor(parentScreen.getMagicData());
        String text = "locked";
        if (access.canModify()) {
            text = null;
        }else if (access.canViewInner()) {
            text = "read_only";
        }
        if (text != null) {
            graphics.centeredText(Minecraft.getInstance().font, text, this.getX(), this.getY(), 0xFFF4E511);
        }
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractWidgetRenderState(guiGraphics, mouseX, mouseY, partialTick);

        // ここで通常のノードとは違う「四角い箱型」の見た目を描画する！
        int bgColor = this.isHovered ? 0xFF334466 : 0xFF222233;
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + height, bgColor); // 箱の高さやデザインを自由にカスタム
        guiGraphics.outline(this.getX(), this.getY(), this.width, height, 0xFF66AACC);

        // タイトル文字の描画
        guiGraphics.centeredText(Minecraft.getInstance().font, this.customName, this.getX() + (this.width / 2), this.getY() + 16, 0xFFFFFF55);
        renderLockedOverLay(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        //TODO nanikore
    }
}
