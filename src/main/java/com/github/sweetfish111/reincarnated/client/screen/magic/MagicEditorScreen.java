package com.github.sweetfish111.reincarnated.client.screen.magic;

import com.github.sweetfish111.reincarnated.circuit.*;
import com.github.sweetfish111.reincarnated.client.screen.WorkspaceCamera;
import com.github.sweetfish111.reincarnated.item.ReincarnatedItems;
import com.github.sweetfish111.reincarnated.magic.skill.SkillAccessLevel;
import com.github.sweetfish111.reincarnated.network.payload.*;
import com.github.sweetfish111.reincarnated.player.PlayerMagicData;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.util.*;


public class MagicEditorScreen extends AbstractEditorScreen {
    private final Deque<CircuitLayer> layerStack = new ArrayDeque<>();
    private int editingMagicSlot = 0;
    private MagiculeCircuit workCircuit = new MagiculeCircuit();
    private String errorMessage = null;
    private int errorTimer = 0;
    private Button backBtn;
    private EditorTab currentTab = EditorTab.MAGIC;

    private final List<AbstructDraggingNodeWidget> nodeWidgets = new ArrayList<>();

    private EditBox popupBox;

    private Button exportBtn;
    private Button importBtn;
    private final List<Button> magicSlotBtns = new ArrayList<>();
    private final List<Button> magicSlotToggleBtns = new ArrayList<>();

    private boolean isNamingCompound;
    private final List<UUID> collapseTargets = new ArrayList<>();

    private final WorkspaceCamera camera = new WorkspaceCamera();
    private final NodePaletteWidget palette = new NodePaletteWidget(this);

    private Set<AbstructDraggingNodeWidget> activeNodes = new HashSet<>();

    private static final Logger LOGGER = LogUtils.getLogger();

    private final PlayerMagicData magicData;



    public MagicEditorScreen(PlayerMagicData magicData){
        super(Component.literal("魔法編集"));
        this.magicData = magicData;
    }

    public PlayerMagicData getMagicData(){return this.magicData;}
    public int getEditingMagicSlot(){ return editingMagicSlot; }
    public String getErrorMessage(){return this.errorMessage;}
    public Button getBackBtn(){return this.backBtn;}
    public MagiculeCircuit getWorkCircuit(){return this.workCircuit;}
    public Deque<CircuitLayer> getLayerStack(){return this.layerStack;}
    public EditorTab getCurrentTab(){return this.currentTab;}
    public void setBackBtn(Button backBtn) {this.backBtn = backBtn;}


    public void setCollapseTargets(List<UUID> collapseTargets){
        this.collapseTargets.clear();
        this.collapseTargets.addAll(collapseTargets);
    }

    //初期化
    @Override
    protected void init(){
        super.init();
        this.layerStack.clear();
        this.workCircuit = this.magicData.getMagicSlot(this.editingMagicSlot);


        super.initTabBtns(EditorTab.MAGIC);

        Button backBtn = Button.builder(
                Component.literal("<- 戻る"),
                button -> goBackLayer()
        ).bounds(this.width - 60 -20 -60, 5, 60, 20).build();
        setBackBtn(backBtn);
        addRenderableWidget(backBtn);

        updateBackButtonVisibility();

        // 画面の右上座標を計算 (例: 画面右端から少し内側、上部から少し下)
        int buttonWidth = 60;
        int buttonHeight = 20;
        int posX = this.width - buttonWidth - 10;
        int posY = this.height - buttonHeight - 10;

        // プレイヤーが手に「白紙の本」を持っているかチェック
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            ItemStack heldItem = minecraft.player.getMainHandItem();

            if (heldItem.is(Items.BOOK)) {
                // 右上に「エクスポート」ボタンを追加
                exportBtn = Button.builder(Component.literal("💾 焼き付け"), button -> {
                    // 現在のワークスペースの回路をNBT化
                    CompoundTag circuitTag = this.getWorkCircuit().saveToNBT();

                    // サーバーへパケット送信
                    ExportSpellPalyload payload = new ExportSpellPalyload(circuitTag);
                    if(net.minecraft.client.Minecraft.getInstance().getConnection() != null){
                        net.minecraft.client.Minecraft.getInstance().getConnection().send(payload);
                    }
                }).bounds(posX, posY, buttonWidth, buttonHeight).build();

                this.addRenderableWidget(exportBtn);
            }else if(heldItem.is(ReincarnatedItems.GRIMOIRE)){
                CustomData data = heldItem.get(DataComponents.CUSTOM_DATA);
                CompoundTag circuitTag = data.copyTag();
                importBtn = Button.builder(Component.literal("魔道書から学ぶ"), button -> {
                    MagiculeCircuit currentCircuit = this.getWorkCircuit();
                    currentCircuit.loadFromNBT(circuitTag);
                    rebuildNodeWidgets();
                }).bounds(posX, posY, buttonWidth, buttonHeight).build();

                this.addRenderableWidget(importBtn);
            }
        }
        rebuildMagicSlotButtons();

        rebuildNodeWidgets();
    }

    @Override
    protected void onTabSelected(EditorTab tab) {
        switchTab(tab);
        for (int i  = 0; i < EditorTab.values().length; i++){
            if(i < this.tabBtns.size()){
                this.tabBtns.get(i).active = (EditorTab.values()[i] != tab);
            }
        }
    }

    public void triggerError(MutableComponent message){
        this.errorMessage = message.getString();
        this.errorTimer = 60;
    }

    private void rebuildMagicSlotButtons() {
        for (Button btn : magicSlotBtns) {
            this.removeWidget(btn);
        }
        magicSlotBtns.clear();

        for (Button btn : magicSlotToggleBtns) {
            this.removeWidget(btn);
        }
        magicSlotToggleBtns.clear();

        if (currentTab != EditorTab.MAGIC) return;

        int startX = 10;
        int y = 28;
        for (int i = 0; i < PlayerMagicData.MAGIC_SLOT_COUNT; i++) {
            final int slotIndex = i;

            // スロット選択ボタン（既存）
            Button slotBtn = Button.builder(
                    Component.literal(String.valueOf(i + 1)),
                    button -> {
                        switchMagicSlot(slotIndex, this.nodeWidgets);
                        clearCanvasWidgets();
                        rebuildNodeWidgets();
                        rebuildMagicSlotButtons();
                    }
            ).bounds(startX, y, 30, 20).build();

            slotBtn.active = (slotIndex != getEditingMagicSlot());
            this.magicSlotBtns.add(slotBtn);
            this.addRenderableWidget(slotBtn);

            // パッシブ有効化トグルボタン（新規）
            boolean enabled = this.magicData.isMagicSlotEnabled(slotIndex);
            Component toggleLabel = enabled
                    ? Component.literal("ON").withColor(TextColor.GREEN)
                    : Component.literal("OFF").withColor(TextColor.RED);

            Button toggleBtn = Button.builder(toggleLabel, button -> {
                boolean newState = !this.magicData.isMagicSlotEnabled(slotIndex);
                this.magicData.setMagicSlotEnabled(slotIndex, newState); // クライアント側の見た目を即時反映

                ToggleMagicSlotPayload payload = new ToggleMagicSlotPayload(slotIndex, newState);
                if (Minecraft.getInstance().getConnection() != null) {
                    Minecraft.getInstance().getConnection().send(payload);
                }

                rebuildMagicSlotButtons(); // ラベル色を更新するため再構築
            }).bounds(startX, y + 22, 30, 16).build();

            this.magicSlotToggleBtns.add(toggleBtn);
            this.addRenderableWidget(toggleBtn);

            startX += 34;
        }
    }

    public void switchTab(EditorTab tab){
        saveCurrentTabCircuit(this.nodeWidgets);
        if(this.currentTab == tab)return;

        this.currentTab = tab;
        this.layerStack.clear();

        this.updateBackButtonVisibility();
        clearCanvasWidgets();
        rebuildNodeWidgets();
        rebuildMagicSlotButtons();

        if(Minecraft.getInstance().getConnection() != null){
            Minecraft.getInstance().getConnection().send(new SwitchTabToSkillPayload());
        }

    }

    public void loadTabCircuit(EditorTab tab){
        if (tab == EditorTab.MAGIC) {
            this.workCircuit = this.magicData.getMagicSlot(this.editingMagicSlot);
        } else {
            this.workCircuit = this.magicData.getCircuit(tab);
        }
    }

    public void switchMagicSlot(int slotIndex, List<AbstructDraggingNodeWidget> nodeWidgets){
        if (slotIndex < 0 || slotIndex >= PlayerMagicData.MAGIC_SLOT_COUNT) return;
        if (slotIndex == editingMagicSlot) return;

        saveCurrentTabCircuit(nodeWidgets); // 今のスロットの編集内容を保存
        this.editingMagicSlot = slotIndex;
        this.layerStack.clear();
        loadTabCircuit(EditorTab.MAGIC);
        updateBackButtonVisibility();
    }

    private void clearCanvasWidgets(){
        for(AbstructDraggingNodeWidget nodeWidget : this.nodeWidgets){
            if(nodeWidget.getContentWidget() != null){
                this.removeWidget(nodeWidget.getContentWidget().getContentWidget());
            }
            this.removeWidget(nodeWidget);
        }
        this.nodeWidgets.clear();
    }

    public void rebuildNodeWidgets(){
        System.out.println("MagicEditorScreen_rebuild_start : " + getWorkCircuit());
        clearCanvasWidgets();
        palette.close();
        for(MagiculeCircuit.NodeData nodeData : getWorkCircuit().getNodes()){
            AbstructDraggingNodeWidget nodeWidget = new DraggableNodeWidget(
                    this,
                    nodeData.id,
                    nodeData.x,
                    nodeData.y,
                    80,
                    nodeData.type
            );
            this.nodeWidgets.add(nodeWidget);
        }
        for(MagiculeCircuit.CompoundNodeData compoundNodeData : getWorkCircuit().getCompoundNodes()){
            CompoundNodeWidget compoundWidget = new CompoundNodeWidget(
                    this,
                    compoundNodeData.id,
                    compoundNodeData.x,
                    compoundNodeData.y,
                    80,
                    compoundNodeData.customName
            );
            this.nodeWidgets.add(compoundWidget);
        }
    }

    //ポートにつながって存在が確定したワイヤーを記録する
    public void onWireDropped(AbstructDraggingNodeWidget sourceNode, NodePort sourcePort, double dropX, double dropY){
        if(sourcePort.getType() != PortType.OUTPUT) return;

        for(AbstructDraggingNodeWidget targetNode : this.nodeWidgets){
            if(targetNode != sourceNode){
                for(NodePort targetPort : targetNode.getInputPorts()){
                    if(targetPort.isMouseOver(dropX, dropY)){
                        if(sourcePort.getDataType() != targetPort.getDataType() && (! sourcePort.getDataType().equals(PortDataType.ANY)) && (! targetPort.getDataType().equals(PortDataType.ANY)) ){
                            LOGGER.info("ポートの型が違うため接続できません");
                            return;
                        }
                        getWorkCircuit().addWire(sourceNode.getId(), sourcePort.getIndex(), targetNode.getId(),targetPort.getIndex(), sourcePort.getDataType() != PortDataType.EXEC && targetPort.getDataType() != PortDataType.EXEC);

                        return;
                    }
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.errorTimer > 0) {
            this.errorTimer--;
            if (this.errorTimer <= 0) {
                this.errorMessage = null;
            }
        }
    }

    //レンダリングメソッド
    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {

        double canvasMouseX = this.camera.getCanvasX(mouseX);
        double canvasMouseY = this.camera.getCanvasY(mouseY);

        //ズーム適用
        guiGraphicsExtractor.pose().pushMatrix();
        guiGraphicsExtractor.pose().translate((float)this.camera.panX, (float)this.camera.panY);
        guiGraphicsExtractor.pose().scale(this.camera.zoom, this.camera.zoom);
        //ワイヤー描画
        for(MagiculeCircuit.WireData wire : getWorkCircuit().getWires()){
            AbstructDraggingNodeWidget source = findNodeById(wire.sourceId);
            AbstructDraggingNodeWidget target = findNodeById(wire.targetId);
            if(source != null && target != null) {
                if (wire.sourcePortIndex >= 0 && wire.sourcePortIndex < source.getOutputPorts().size() &&
                        wire.targetPortIndex >= 0 && wire.targetPortIndex < target.getInputPorts().size()) {
                    NodePort outPort = source.getOutputPorts().get(wire.sourcePortIndex);
                    NodePort inPort = target.getInputPorts().get(wire.targetPortIndex);
                    boolean isDataFlow = (outPort.getDataType() != PortDataType.EXEC);
                    drawMagiculeWire(guiGraphicsExtractor, outPort.getX() + 3, outPort.getY() + 3, inPort.getX() + 3, inPort.getY() + 3, isDataFlow);
                }
            }
        }
        //ノード描画
        for(AbstructDraggingNodeWidget nodeWidget : this.nodeWidgets){
            nodeWidget.extractRenderState(guiGraphicsExtractor, (int)canvasMouseX, (int)canvasMouseY, partialTick);
            if(nodeWidget.getContentWidget() != null){
                nodeWidget.getContentWidget().getContentWidget().extractRenderState(guiGraphicsExtractor, (int)canvasMouseX, (int)canvasMouseY, partialTick);
            }
        }

//        画面のズーム適用範囲外
        guiGraphicsExtractor.pose().popMatrix();



        String errorMsg = this.getErrorMessage();
        if (errorMsg != null) {
            int screenWidth = this.width;

            // 画面中央上部に赤字でアナウンスを表示
            guiGraphicsExtractor.centeredText(
                    this.font,
                    Component.literal(errorMsg),
                    screenWidth / 2,
                    40, // 画面上からの高さ
                    0xFFFF5555 // 赤色のカラーコード
            );
        }

        //ボタン描画
        this.palette.render(guiGraphicsExtractor, mouseX, mouseY);
        if(this.isNamingCompound && this.popupBox != null){
            this.popupBox.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTick);
        }

        super.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        //ズーム未適用座標
        double rawX = event.x();
        double rawY = event.y();
        //ズーム適応座標
        double canvasX = this.camera.getCanvasX(event.x());
        double canvasY = this.camera.getCanvasY(event.y());

        if(this.isNamingCompound && this.popupBox != null){
            this.popupBox.onClick(event, doubleClick);
            return true;
        }

        super.clickTabButton(event, doubleClick);

        if(getBackBtn().mouseClicked(event, doubleClick)){
            return true;
        }

        //exportBtnのクリック判定
        if(exportBtn != null && exportBtn.mouseClicked(event, doubleClick)){
            return true;
        }

        //importBtnのクリック判定
        if(importBtn != null && importBtn.mouseClicked(event, doubleClick)){
            return true;
        }

        for(Button btn : magicSlotBtns){
            if(btn.mouseClicked(event, doubleClick)){
                return true;
            }
        }

        for(Button btn : magicSlotToggleBtns){
            if(btn.mouseClicked(event,doubleClick)){
                return true;
            }
        }

        //パレットのクリック判定
        if(palette.isOpen()){
            palette.mouseClicked(rawX, rawY, event.button());
        }

        //ノードのクリック＆右クリック判定
        for(int i = nodeWidgets.size() - 1; i >= 0; i--){
            AbstructDraggingNodeWidget node = nodeWidgets.get(i);
            if(node.handleCanvasClick(event, canvasX, canvasY)){
                //クリックしたノードを最前面に移動
                nodeWidgets.remove(i);
                nodeWidgets.add(node);

                if(doubleClick && node instanceof CompoundNodeWidget cNode){
                    diveLayer(cNode, nodeWidgets, magicData);
                    rebuildNodeWidgets();
                }

                if(event.hasControlDown()){
                    if(event.button() == 0){
                        activeNodes.add(node);
                        node.setFocused(true);
                        node.setDragging(true);
                        for (AbstructDraggingNodeWidget active : activeNodes){
                            active.setDragOffset(canvasX, canvasY);
                        }
                    }else if(event.button() == 1){
                        activeNodes.add(node);
                        node.setFocused(true);
                        node.setDragging(true);
                        palette.openContextMenu((int) rawX, (int) rawY, activeNodes);
                    }
                }else if(event.button() == 1){
                    if(activeNodes.contains(node)){
                        palette.openContextMenu((int)rawX, (int)rawY, activeNodes);
                        return true;
                    }

                    for (AbstructDraggingNodeWidget active : activeNodes){
                        active.setFocused(false);
                        active.setDragging(false);
                    }
                    activeNodes.clear();

                    if(node.portClicked((int)canvasX, (int)canvasY, event.button())){
                        return true;

                    }

                    activeNodes.add(node);
                    node.setFocused(true);
                    palette.openContextMenu((int)rawX, (int)rawY, activeNodes);
                    return true;

                }else if(event.button() == 0){
                    if(activeNodes.contains(node)){
                        for (AbstructDraggingNodeWidget active : activeNodes){
                            active.setDragOffset(canvasX, canvasY);
                        }
                        return true;
                    }else{
                        for(AbstructDraggingNodeWidget active : activeNodes){
                            active.setDragging(false);
                            active.setFocused(false);
                        }
                        activeNodes.clear();
                        node.setDragging(true);
                        node.setDragOffset(canvasX, canvasY);
                        return true;
                    }
                }
                return true;
            }
        }

        //何もない空間のクリック
        if(event.button() == 1){
            this.palette.openPalette((int)rawX, (int)rawY, canvasX, canvasY);
            return true;
        }else if(event.button() == 0){
            for (AbstructDraggingNodeWidget active : activeNodes){
                active.setFocused(false);
                active.setDragging(false);
            }
            activeNodes.clear();
        }

        return this.camera.mouseClicked(event.x(), event.y(), event.button());
    }

    public void goBackLayer(){
        if(layerStack.isEmpty())return;

        CircuitLayer parentLayer = layerStack.pop();
        saveCurrentInnerCircuit(parentLayer, nodeWidgets);

        this.workCircuit = parentLayer.parentCircuit;
        updateBackButtonVisibility();
        rebuildNodeWidgets();
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

        if (this.currentTab == EditorTab.MAGIC) {
            this.magicData.setMagicSlot(editingMagicSlot, this.workCircuit);
        } else {
            this.magicData.setCircuits(this.currentTab, this.workCircuit);
        }
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

    public void copyNode(AbstructDraggingNodeWidget node){
        if(node instanceof DraggableNodeWidget dNode){
            if(dNode.getContentWidget() != null){
                spawnNodeWithParam(dNode.getType(), dNode.getX() + 10, dNode.getY() + 10, dNode.getContentWidget().getCurrentValue());
            }else{
                spawnNode(dNode.getType(), dNode.getX() + 10, dNode.getY() + 10);
            }
        }else if(node instanceof CompoundNodeWidget cNode){
            if(cNode.getContentWidget() != null){
                SkillAccessLevel currentAccess = cNode.getLinkedData().getAccessLevelFor(magicData);
                if (!currentAccess.canModify()) {
                    triggerError(Component.translatable("message.reincarnated.compound_accessDenied"));
                    return;
                }
                MagiculeCircuit.CompoundNodeData clonedData = new MagiculeCircuit.CompoundNodeData(
                        UUID.randomUUID(),
                        cNode.getCustomName(),
                        cNode.getLinkedData().getCompoundCircuit().getNodes(),
                        cNode.getLinkedData().getCompoundCircuit().getCompoundNodes(),
                        cNode.getLinkedData().getCompoundCircuit().getWires(),
                        cNode.getLinkedData().getCompoundCircuit().getNodeParameters(),
                        cNode.getX() + 10,
                        cNode.getY() + 10
                );
                this.getWorkCircuit().addCompoundNode(clonedData);
            }else{
                SkillAccessLevel currentAccess = cNode.getLinkedData().getAccessLevelFor(magicData);
                if (!currentAccess.canModify()) {
                    triggerError(Component.translatable("message.reincarnated.compound_accessDenied"));
                    return;
                }
                MagiculeCircuit.CompoundNodeData clonedData = new MagiculeCircuit.CompoundNodeData(
                        UUID.randomUUID(),
                        cNode.getCustomName(),
                        cNode.getLinkedData().getCompoundCircuit().getNodes(),
                        cNode.getLinkedData().getCompoundCircuit().getCompoundNodes(),
                        cNode.getLinkedData().getCompoundCircuit().getWires(),
                        cNode.getLinkedData().getCompoundCircuit().getNodeParameters(),
                        cNode.getX() + 10,
                        cNode.getY() + 10
                );
                this.getWorkCircuit().addCompoundNode(clonedData);
            }
            rebuildNodeWidgets();
        }
    }

    //何もない空間がドラッグされたらキャンバス内を移動する
    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY){
        double canvasX = this.camera.getCanvasX(event.x());
        double canvasY = this.camera.getCanvasY(event.y());

        //dragもズーム影響を受けるため変換
        double canvasDragX = dragX / this.camera.zoom;
        double canvasDragY = dragY / this.camera.zoom;

        for(AbstructDraggingNodeWidget node : nodeWidgets){
            node.handleCanvasDragged(canvasX, canvasY, canvasDragX, canvasDragY);
        }
        return this.camera.mouseDragged(dragX,dragY);
    }

    //マウスが離されたら初期状態に戻る
    @Override
    public boolean mouseReleased(MouseButtonEvent event){
        double canvasX = this.camera.getCanvasX(event.x());
        double canvasY = this.camera.getCanvasY(event.y());
        if(!event.hasControlDown()){
            for(AbstructDraggingNodeWidget node : nodeWidgets){
                node.handleCanvasReleased(canvasX, canvasY, event.button());
            }
        }

        this.camera.mouseReleased();
        return true;
    }

    //スクロールはズームインとズームアウト
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.palette.isOpen()) {
            if (this.palette.mouseScrolled(mouseX, mouseY, scrollY)) {
                return true;
            }
        }
        this.camera.zoomAt(mouseX, mouseY, scrollY);
        return true;
    }

    //魔法編集画面全体を閉じたとき
    @Override
    public void onClose() {
        saveCurrentInnerCircuit(getLayerStack().peek(), this.nodeWidgets);
        saveCurrentTabCircuit(this.nodeWidgets);

        CompoundTag rootTag = this.magicData.saveToNBT();

        LOGGER.info("===サーバーへのpayloadの送信===");
        SaveCircuitPayload payload = new SaveCircuitPayload(rootTag);
        if(net.minecraft.client.Minecraft.getInstance().getConnection() != null){
            net.minecraft.client.Minecraft.getInstance().getConnection().send(payload);
        }

        super.onClose();
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent event) {
        for (AbstructDraggingNodeWidget node : this.nodeWidgets) {
            if (node.getContentWidget() != null && node.keyPressed(event)) {
                return true;
            }
        }
        if(this.isNamingCompound && this.popupBox != null){
            if(this.popupBox.keyPressed(event)){
                return true;
            }
            if(event.key() == 257 || event.key() == 335){
                String customName = this.popupBox.getValue();
                this.removeWidget(this.popupBox);
                this.popupBox = null;
                this.isNamingCompound = false;
                this.getWorkCircuit().collapseNodes(collapseTargets, customName);
                this.rebuildNodeWidgets();
                return true;
            }

            if(event.key() == 256){
                this.removeWidget(this.popupBox);
                this.popupBox = null;
                this.isNamingCompound = false;
                return true;
            }
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(@NonNull CharacterEvent event) {
        for (AbstructDraggingNodeWidget node : this.nodeWidgets) {
            if (node.getContentWidget() != null && node.charTyped(event)) {
                return true;
            }
        }
        if(this.isNamingCompound && this.popupBox != null){
            if(this.popupBox.charTyped(event)){
                return true;
            }
        }
        return super.charTyped(event);
    }

    //ノードの削除
    public void deleteNode(AbstructDraggingNodeWidget node){
        if(node instanceof CompoundNodeWidget cNode){
            SkillAccessLevel access = cNode.getLinkedData().getAccessLevelFor(magicData);
            if (!access.canModify()) {
                triggerError(Component.translatable("message.reincarnated.compound_accessDenied"));
                return;
            }
        }
        this.nodeWidgets.remove(node);
        this.removeWidget(node);
        getWorkCircuit().removeNodeAndWires(node.getId());
        if(node.getContentWidget() != null){
            this.removeWidget(node.getContentWidget());
        }
    }

    //画面に新しいノードを誕生させる
    public void spawnNode(MagiculeNodeType type, double canvasX, double canvasY){
        Object initialVal = (type.getContent() == ContentWidgetType.SWITCH) ? false : 0.0;
        spawnNodeWithParam(type, canvasX, canvasY, initialVal);
    }
    public void spawnNodeWithParam(MagiculeNodeType type, double canvasX, double canvasY, Object initialValue) {
        UUID newId = UUID.randomUUID();

        getWorkCircuit().addNode(new MagiculeCircuit.NodeData(newId, type, (int) canvasX, (int) canvasY));
        getWorkCircuit().setNodeParam(newId, "value", initialValue);

        DraggableNodeWidget newNode = new DraggableNodeWidget(this, newId, (int) canvasX, (int) canvasY, 80, type);
        this.nodeWidgets.add(newNode);
    }

    //画面上のノードリストからid指定に合致するものを返す
    public AbstructDraggingNodeWidget findNodeById(UUID id){
        for(AbstructDraggingNodeWidget node : this.nodeWidgets){
            if(node.getId().equals(id)) return node;
        }
        return null;
    }

    //ポートの接続ワイヤの描画メソッド
    private void drawMagiculeWire(GuiGraphicsExtractor guiGraphicsExtractor, int startX, int startY, int endX, int endY, boolean wireType){
        int dx = endX - startX;
        int dy = endY - startY;
        int steps = Math.max(Math.abs(dx), Math.abs(dy)) / 4;
        if(steps == 0) return;
        float xInc = (float) dx / steps;
        float yInc = (float) dy / steps;
        float x = startX;
        float y = startY;
        int color = wireType ? 0xFF00AAFF : 0xFFFFFFFF;
        for (int i = 0; i <= steps; i++){
            guiGraphicsExtractor.fill((int)x, (int)y, (int)x + 2, (int)y + 2, color);
            x += xInc;
            y += yInc;
        }
    }

    public void openCompoundNamingPopup(){
        this.isNamingCompound = true;
        this.popupBox = new EditBox(
                Minecraft.getInstance().font,
                this.width / 2 - 60, this.height / 2 - 10,
                120, 20,
                Component.literal("Enter Custom Name")
        );
        this.popupBox.setMaxLength(15);
        this.popupBox.setValue("MyCustomMagic");

        this.setFocused(this.popupBox);
        this.popupBox.setFocused(true);

        this.addRenderableWidget(this.popupBox);
    }

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
