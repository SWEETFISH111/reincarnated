package com.github.sweetfish111.reincarnated.client.screen;

import com.github.sweetfish111.reincarnated.circuit.EditorTab;
import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.magic.slill.SkillAccessLevel;
import com.github.sweetfish111.reincarnated.player.PlayerMagicData;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.common.Mod;

import java.util.*;


public class ScreenLayerManager {
    private final Deque<CircuitLayer> layerStack = new ArrayDeque<>();
    private EditorTab currentTab = EditorTab.MAGIC;
    private MagiculeCircuit workCircuit = new MagiculeCircuit();
    private PlayerMagicData magicData;
    private List<Button> tabBtns = new ArrayList<>();
    private Button backBtn;
    private String errorMessage = null;
    private int errorTimer = 0;


    public void init(PlayerMagicData magicData) {
        this.layerStack.clear();
        this.magicData = magicData;
        this.workCircuit = this.magicData.getCircuit(this.currentTab);
    }

    public String getErrorMessage(){return this.errorMessage;}
    public List<Button> getTabBtns(){return this.tabBtns;}
    public Button getBackBtn(){return this.backBtn;}
    public EditorTab getCurrentTab(){return this.currentTab;}
    public MagiculeCircuit getWorkCircuit(){return this.workCircuit;}
    public Deque<CircuitLayer> getLayerStack(){return this.layerStack;}
    public void setBackBtn(Button backBtn) {this.backBtn = backBtn;}

    public void triggerError(MutableComponent message){
        this.errorMessage = message.getString();
        this.errorTimer = 60;
    }

    public void tick(){
        if (this.errorTimer > 0) {
            this.errorTimer--;
            if (this.errorTimer <= 0) {
                this.errorMessage = null;
            }
        }
    }

    public void switchTab(EditorTab tab) {
        if(this.currentTab == tab)return;

        this.currentTab = tab;
        this.layerStack.clear();

        for (int i  = 0; i < EditorTab.values().length; i++){
            if(i < this.tabBtns.size()){
                this.tabBtns.get(i).active = (EditorTab.values()[i] != this.currentTab);
            }
        }
        this.loadTabCircuit(this.currentTab);
        this.updateBackButtonVisibility();
    }

    public void loadTabCircuit(EditorTab tab){
        this.workCircuit = this.magicData.getCircuit(tab);
    }

    public void goBackLayer(List<AbstructDraggingNodeWidget> nodeWidgets){
        if(layerStack.isEmpty())return;

        CircuitLayer parentLayer = layerStack.pop();
        saveCurrentInnerCircuit(parentLayer, nodeWidgets);

        this.workCircuit = parentLayer.parentCircuit;
        updateBackButtonVisibility();
    }

    public void updateBackButtonVisibility(){
        if(this.backBtn != null){
            this.backBtn.visible = !layerStack.isEmpty();
        }
    }

    public void saveCurrentTabCircuit(List<AbstructDraggingNodeWidget> nodeWidgets){
        if(!this.layerStack.isEmpty())return;

        this.workCircuit.getNodes().clear();
        List<MagiculeCircuit.CompoundNodeData> updatedCompounds = new ArrayList<>();
        for(AbstructDraggingNodeWidget widget : nodeWidgets){

            if(widget instanceof CompoundNodeWidget compoundWidget){
                if(compoundWidget != null){
                    MagiculeCircuit.CompoundNodeData existingData = findCompoundDataById(compoundWidget.getId());
                    if(existingData != null){
                        existingData.x = compoundWidget.getX();
                        existingData.y = compoundWidget.getY();
                        updatedCompounds.add(existingData);
                    }
                }

            }else if(widget instanceof DraggableNodeWidget draggableNodeWidget){
                this.workCircuit.addNode(new MagiculeCircuit.NodeData(
                        draggableNodeWidget.getId(),
                        draggableNodeWidget.getType(),
                        draggableNodeWidget.getX(),
                        draggableNodeWidget.getY()
                ));
                Object val = draggableNodeWidget.getContentWidget() != null ? draggableNodeWidget.getContentWidget().getCurrentValue() : null;
                if(val != null){
                    this.workCircuit.setNodeParam(draggableNodeWidget.getId(), "value", val);
                }
            }
        }

        this.workCircuit.setCompoundNodes(updatedCompounds);
        this.magicData.setCircuits(this.currentTab, this.workCircuit);
    }

    public MagiculeCircuit.CompoundNodeData findCompoundDataById(UUID id){
        for(MagiculeCircuit.CompoundNodeData data : this.workCircuit.getCompoundNodes()){
            if(data.id.equals(id))return data;
        }
        return null;
    }

    public boolean diveLayer(AbstructDraggingNodeWidget node, List<AbstructDraggingNodeWidget> nodeWidgets, PlayerMagicData magicData){
        if(node instanceof CompoundNodeWidget cNode){
            SkillAccessLevel access = cNode.getLinkedData().getAccessLevelFor(magicData);
            if (!access.canViewInner()) {
                triggerError(Component.translatable("message.reincarnated.compound_accessDenied"));
                return false;
            }
        }
        MagiculeCircuit.CompoundNodeData compoundData = findCompoundDataById(node.getId());
        if(compoundData != null){
            saveCurrentTabCircuit(nodeWidgets);

            MagiculeCircuit innerCircuit = new MagiculeCircuit();
            for(MagiculeCircuit.NodeData nodeData : compoundData.getCompoundCircuit().getNodes()){
                innerCircuit.addNode(nodeData);
            }
            for (MagiculeCircuit.CompoundNodeData innerCompound : compoundData.getCompoundCircuit().getCompoundNodes()){
                innerCircuit.getCompoundNodes().add(innerCompound);
            }
            for(MagiculeCircuit.WireData wireData : compoundData.getCompoundCircuit().getWires()){
                innerCircuit.addWire(wireData.sourceId, wireData.sourcePortIndex, wireData.targetId, wireData.targetPortIndex, wireData.isDataFlow);
            }

            for(Map.Entry<UUID, Map<String, Object>> entry : compoundData.getCompoundCircuit().getNodeParameters().entrySet()){
                UUID nId = entry.getKey();
                for(Map.Entry<String, Object> paramEntry : entry.getValue().entrySet()){
                    innerCircuit.setNodeParam(nId, paramEntry.getKey(), paramEntry.getValue());
                }
            }

            layerStack.push(new CircuitLayer(
                    innerCircuit,
                    compoundData.customName,
                    compoundData.id,
                    this.workCircuit
            ));


            this.workCircuit = innerCircuit;
            updateBackButtonVisibility();
            return true;
        }
        return false;
    }

    public void saveCurrentInnerCircuit(CircuitLayer currentLayer, List<AbstructDraggingNodeWidget>nodeWidgets){
        if(currentLayer == null || currentLayer.parentCompoundId == null) return;

        for(MagiculeCircuit.CompoundNodeData cNode : currentLayer.parentCircuit.getCompoundNodes()){
            if(cNode.id.equals(currentLayer.parentCompoundId)){
                cNode.getCompoundCircuit().getNodes().clear();
                cNode.getCompoundCircuit().getCompoundNodes().clear();
                cNode.getCompoundCircuit().getWires().clear();
                cNode.getCompoundCircuit().getNodeParameters().clear();

                for(AbstructDraggingNodeWidget widget : nodeWidgets){
                    if(widget instanceof CompoundNodeWidget compoundWidget){
                        MagiculeCircuit.CompoundNodeData existingCompound = findCompoundDataById(compoundWidget.getId());
                        if(existingCompound != null){
                            existingCompound.x = compoundWidget.getX();
                            existingCompound.y = compoundWidget.getY();
                            cNode.getCompoundCircuit().getCompoundNodes().add(existingCompound);
                        }
                    }else if(widget instanceof DraggableNodeWidget dWidget){
                        cNode.getCompoundCircuit().getNodes().add(new MagiculeCircuit.NodeData(
                                dWidget.getId(),
                                dWidget.getType(),
                                dWidget.getX(),
                                dWidget.getY()));

                        Object val = widget.getContentWidget() != null ? widget.getContentWidget().getCurrentValue() : null;
                        if(val != null){
                            cNode.getCompoundCircuit().getNodeParameters().computeIfAbsent(widget.getId(), k -> new HashMap<>()).put("value", val);
                        }
                    }
                }

                for(MagiculeCircuit.WireData wire : this.workCircuit.getWires()){
                    cNode.getCompoundCircuit().getWires().add(wire);
                }

            }

        }
    }

    /*
   =========== 内部データクラス ==========
     */
    public static class CircuitLayer {
        public final MagiculeCircuit workCircuit;
        public final String title;
        public final UUID parentCompoundId;
        public final MagiculeCircuit parentCircuit;

        public CircuitLayer(MagiculeCircuit circuit, String title, UUID parentCompoundId, MagiculeCircuit parentCircuit) {
            this.workCircuit = circuit;
            this.title = title;
            this.parentCompoundId = parentCompoundId;
            this.parentCircuit = parentCircuit;
        }
    }

}
