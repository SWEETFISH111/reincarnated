package com.github.sweetfish111.reincarnated.client.screen;

import com.github.sweetfish111.reincarnated.circuit.*;
import com.github.sweetfish111.reincarnated.network.payload.SaveCircuitPayload;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.util.*;

public class MagicEditorScreen extends Screen {

    private MagiculeCircuit circuit = new MagiculeCircuit();
    private final Map<EditorTab, MagiculeCircuit> tabCircuits = new EnumMap<>(EditorTab.class);

    private final List<DraggableNodeWidget> nodeWidgets = new ArrayList<>();
    private final List<CompoundNodeWidget> compoundNodeWidgets = new ArrayList<>();
    private final List<Button> tabButtons = new ArrayList<>();
    private Button backButton;

    private final WorkspaceCamera camera = new WorkspaceCamera();
    private final NodePaletteWidget palette = new NodePaletteWidget(this);

    private Set<DraggableNodeWidget> activeNodes = new HashSet<>();

    private static final Logger LOGGER = LogUtils.getLogger();

    private final PlayerMagicData magicData;

    public enum EditorTab{
        MAGIC("魔法"),
        SKILL("スキル"),
        ARTS("アーツ");

        private final String displayName;
        EditorTab(String displayName){this.displayName = displayName;}
        public String getDisplayName(){return this.displayName;}
    }

    private EditorTab currentTab = EditorTab.MAGIC;

    public static class CircuitLayer{
        public final MagiculeCircuit circuit;
        public final String title;
        public final UUID parentCompoundId;
        public final MagiculeCircuit parentCircuit;

        public CircuitLayer(MagiculeCircuit circuit, String title, UUID parentCompoundId, MagiculeCircuit parentCircuit){
            this.circuit = circuit;
            this.title = title;
            this.parentCompoundId = parentCompoundId;
            this.parentCircuit = parentCircuit;
        }
    }

    private final Deque<CircuitLayer> layerStack = new ArrayDeque<>();

    public MagicEditorScreen(PlayerMagicData magicData){
        super(Component.literal("魔法編集"));
        this.magicData = magicData;
    }

    //getter
    public MagiculeCircuit getCircuit(){return this.circuit;}
    public EditorTab getCurrentTab(){return this.currentTab;}

    //初期化
    @Override
    protected void init(){
        super.init();

        this.layerStack.clear();
        this.circuit = this.magicData.getCircuit(this.currentTab);

        int tabButtonWidth = 60;
        int tabButtonHeight = 20;
        int startX = 10;
        int startY = 5;
        for(EditorTab tab : EditorTab.values()){
            Button btn = Button.builder(
                    Component.literal(tab.getDisplayName()),
                    button -> switchTab(tab)
            ).bounds(startX, startY, tabButtonWidth, tabButtonHeight).build();

            btn.active = (tab != this.currentTab);

            this.tabButtons.add(btn);
            this.addRenderableWidget(btn);

            startX += tabButtonWidth + 5;
        }
        int backButtonWidth = 80;
        int backButtonHeight = 20;
        this.backButton = Button.builder(
                Component.literal("← 戻る"),
                button -> goBackLayer()
        ).bounds(10, 35, backButtonWidth, backButtonHeight).build();

        updateBackButtonVisibility();
        this.addRenderableWidget(this.backButton);
        rebuildNodeWidgets();
    }

    private void switchTab(EditorTab tab){
        if(this.currentTab == tab)return;
        saveCurrentTabCircuit();

        this.currentTab = tab;
        this.layerStack.clear();

        for(int i = 0; i < EditorTab.values().length; i++){
            if(i < this.tabButtons.size()){
                this.tabButtons.get(i).active = (EditorTab.values()[i] != this.currentTab);
            }
        }
        clearCanvasWidgets();
        loadTabCircuit(this.currentTab);
        updateBackButtonVisibility();
    }

    private void clearCanvasWidgets(){
        for(DraggableNodeWidget nodeWidget : this.nodeWidgets){
            if(nodeWidget.getContentWidget() != null){
                this.removeWidget(nodeWidget.getContentWidget().getContentWidget());
            }
            this.removeWidget(nodeWidget);
        }
        this.nodeWidgets.clear();

        for(CompoundNodeWidget compoundWidget : this.compoundNodeWidgets){
            this.removeWidget(compoundWidget);
        }
        this.compoundNodeWidgets.clear();
    }

    private void saveCurrentTabCircuit(){
        if(!this.layerStack.isEmpty())return;

        MagiculeCircuit currentCircuit = this.magicData.getCircuit(this.currentTab);
        this.circuit.getNodes().clear();
        List<MagiculeCircuit.CompoundNodeData> updatedCompounds = new ArrayList<>();

        for(DraggableNodeWidget widget : this.nodeWidgets){
            if(widget instanceof CompoundNodeWidget compoundWidget){
                MagiculeCircuit.CompoundNodeData existingData = findCompoundDataById(compoundWidget.getId());;
                if(existingData != null){
                    existingData.x = compoundWidget.getX();
                    existingData.y = compoundWidget.getY();
                    updatedCompounds.add(existingData);
                }
            }else{
                this.circuit.addNode(new MagiculeCircuit.NodeData(
                        widget.getId(),
                        widget.getType(),
                        widget.getX(),
                        widget.getY()
                ));
            }
        }
        this.circuit.setCompoundNodes(updatedCompounds);
        this.magicData.setCircuits(this.currentTab, this.circuit);
    }

    private MagiculeCircuit.CompoundNodeData findCompoundDataById(UUID id){
        for(MagiculeCircuit.CompoundNodeData data : this.circuit.getCompoundNodes()){
            if(data.id.equals(id))return data;
        }
        return null;
    }

    private void loadTabCircuit(EditorTab tab){
        this.circuit = this.magicData.getCircuit(tab);
        rebuildNodeWidgets();
    }

    public void rebuildNodeWidgets(){
        clearCanvasWidgets();
        for(MagiculeCircuit.NodeData nodeData : this.circuit.getNodes()){
            DraggableNodeWidget nodeWidget = new DraggableNodeWidget(
                    this,
                    nodeData.id,
                    nodeData.type,
                    nodeData.x,
                    nodeData.y,
                    80
            );
            this.nodeWidgets.add(nodeWidget);
            this.addRenderableWidget(nodeWidget);
            if(nodeWidget.getContentWidget() != null){
                this.addRenderableWidget(nodeWidget.getContentWidget().getContentWidget());
            }
        }
        for(MagiculeCircuit.CompoundNodeData compoundNodeData : this.circuit.getCompoundNodes()){
            CompoundNodeWidget compoundWidget = new CompoundNodeWidget(
                    this,
                    compoundNodeData.id,
                    MagiculeNodeType.COMPOUND,
                    compoundNodeData.x,
                    compoundNodeData.y,
                    120
            );
            this.compoundNodeWidgets.add(compoundWidget);
            this.nodeWidgets.add(compoundWidget);
            this.addRenderableWidget(compoundWidget);
        }
    }

    //ポートにつながって存在が確定したワイヤーを記録する
    public void onWireDropped(DraggableNodeWidget sourceNode, NodePort sourcePort, double dropX, double dropY){
        if(sourcePort.getType() != NodePort.Type.OUTPUT) return;

        for(DraggableNodeWidget targetNode : this.nodeWidgets){
            if(targetNode != sourceNode){
                for(NodePort targetPort : targetNode.getInputPorts()){
                    if(targetPort.isMouseOver(dropX, dropY)){
                        if(sourcePort.getDataType() != targetPort.getDataType() && (! sourcePort.getDataType().equals(PortDataType.ANY)) && (! targetPort.getDataType().equals(PortDataType.ANY)) ){
                            LOGGER.info("ポートの型が違うため接続できません");
                            return;
                        }
                        this.circuit.addWire(sourceNode.getId(), sourcePort.getIndex(), targetNode.getId(),targetPort.getIndex(), sourcePort.getDataType() != PortDataType.EXEC);

                        return;
                    }
                }
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
        for(MagiculeCircuit.WireData wire : this.circuit.getWires()){
            DraggableNodeWidget source = findNodeById(wire.sourceId);
            DraggableNodeWidget target = findNodeById(wire.targetId);
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
        for(DraggableNodeWidget nodeWidget : this.nodeWidgets){
            nodeWidget.extractRenderState(guiGraphicsExtractor, (int)canvasMouseX, (int)canvasMouseY, partialTick);
            if(nodeWidget.getContentWidget() != null){
                nodeWidget.getContentWidget().getContentWidget().extractRenderState(guiGraphicsExtractor, (int)canvasMouseX, (int)canvasMouseY, partialTick);
            }
        }
        guiGraphicsExtractor.pose().popMatrix();
        for(Button btn : this.tabButtons){
            btn.extractRenderState(guiGraphicsExtractor, (int)canvasMouseX, (int)canvasMouseY, partialTick);
        }
        if(this.backButton != null){
            this.backButton.extractRenderState(guiGraphicsExtractor, (int) canvasMouseX, (int) canvasMouseY, partialTick);
        }
        this.palette.render(guiGraphicsExtractor, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        //ズーム未適用座標
        double rawX = event.x();
        double rawY = event.y();
        //ズーム適応座標
        double canvasX = this.camera.getCanvasX(event.x());
        double canvasY = this.camera.getCanvasY(event.y());

        //タブボタンのクリック判定
        for(Button btn : this.tabButtons){
            if(btn.mouseClicked(event,doubleClick)){
                return true;
            }
        }

        if(this.backButton.mouseClicked(event, doubleClick)){
            return true;
        }

        //パレットのクリック判定
        if(palette.isOpen()){
            palette.mouseClicked(rawX, rawY, event.button());
        }

        //ノードのクリック＆右クリック判定
        for(int i = nodeWidgets.size() - 1; i >= 0; i--){
            DraggableNodeWidget node = nodeWidgets.get(i);
            if(node.handleCanvasClick(event, canvasX, canvasY, event.button())){
                //クリックしたノードを最前面に移動
                nodeWidgets.remove(i);
                nodeWidgets.add(node);

                if(doubleClick && node instanceof CompoundNodeWidget cNode){
                    MagiculeCircuit.CompoundNodeData compoundData = findCompoundDataById(cNode.getId());
                    if(compoundData != null){
                        saveCurrentTabCircuit();
                        layerStack.push(new CircuitLayer(
                                this.circuit,
                                compoundData.customName,
                                compoundData.id,
                                this.circuit
                        ));

                        MagiculeCircuit innerCircuit = new MagiculeCircuit();
                        for(MagiculeCircuit.NodeData nodeData : compoundData.innerNodes){
                            innerCircuit.addNode(nodeData);
                        }
                        for (MagiculeCircuit.CompoundNodeData innerCompound : compoundData.innerCompoundNodes){
                            innerCircuit.getCompoundNodes().add(innerCompound);
                        }
                        for(MagiculeCircuit.WireData wireData : compoundData.innerWires){
                            innerCircuit.addWire(wireData.sourceId, wireData.sourcePortIndex, wireData.targetId, wireData.targetPortIndex, wireData.isDataFlow);
                        }

                        for(Map.Entry<UUID, Map<String, Object>> entry : compoundData.innerNodeParameters.entrySet()){
                            UUID nId = entry.getKey();
                            for(Map.Entry<String, Object> paramEntry : entry.getValue().entrySet()){
                                innerCircuit.setNodeParam(nId, paramEntry.getKey(), paramEntry.getValue());
                            }
                        }

                        this.circuit = innerCircuit;
                        updateBackButtonVisibility();
                        rebuildNodeWidgets();
                        return true;
                    }
                }

                if(event.hasControlDown()){
                    if(event.button() == 0){
                        activeNodes.add(node);
                        node.setActive(true);
                        node.setDragging(true);
                        for (DraggableNodeWidget active : activeNodes){
                            active.setDragOffset(canvasX, canvasY);
                        }
                    }else if(event.button() == 1){
                        activeNodes.add(node);
                        node.setActive(true);
                        node.setDragging(true);
                        palette.openContextMenu((int) rawX, (int) rawY, activeNodes);
                    }
                }else if(event.button() == 1){
                    if(activeNodes.contains(node)){
                        palette.openContextMenu((int)rawX, (int)rawY, activeNodes);
                        return true;
                    }

                    for (DraggableNodeWidget active : activeNodes){
                        active.setActive(false);
                        active.setDragging(false);
                    }
                    activeNodes.clear();

                    if(node.portClicked((int)canvasX, (int)canvasY, event.button())){
                        return true;

                    }

                    activeNodes.add(node);
                    node.setActive(true);
                    palette.openContextMenu((int)rawX, (int)rawY, activeNodes);
                    return true;

                }else if(event.button() == 0){
                    if(activeNodes.contains(node)){
                        for (DraggableNodeWidget active : activeNodes){
                            active.setDragOffset(canvasX, canvasY);
                        }
                        return true;
                    }else{
                        System.out.println("kurikkusita:" + node.getType().getDisplayName());
                        for(DraggableNodeWidget active : activeNodes){
                            active.setDragging(false);
                            active.setActive(false);
                        }
                        activeNodes.clear();
                        node.setDragging(true);
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
            for (DraggableNodeWidget active : activeNodes){
                active.setActive(false);
                active.setDragging(false);
            }
            activeNodes.clear();
        }

        return this.camera.mouseClicked(event.x(), event.y(), event.button());
    }

    public void goBackLayer(){
        if(layerStack.isEmpty())return;

        CircuitLayer parentLayer = layerStack.pop();
        saveCurrentInnerCircuit(parentLayer);

        this.circuit = parentLayer.circuit;
        updateBackButtonVisibility();
        rebuildNodeWidgets();

        LOGGER.info("hitotu ue no kaisou ni modorimasita" + parentLayer.title);
    }

    private void saveCurrentInnerCircuit(CircuitLayer currentLayer){
        if(currentLayer == null || currentLayer.parentCompoundId == null) return;

        for(MagiculeCircuit.CompoundNodeData cNode : currentLayer.parentCircuit.getCompoundNodes()){
            if(cNode.id.equals(currentLayer.parentCompoundId)){
                cNode.innerNodes.clear();
                cNode.innerCompoundNodes.clear();
                cNode.innerWires.clear();
                cNode.innerNodeParameters.clear();

                for(DraggableNodeWidget widget : this.nodeWidgets){
                    if(widget instanceof CompoundNodeWidget compoundWidget){
                        MagiculeCircuit.CompoundNodeData existingCompound = findCompoundDataById(compoundWidget.getId());
                        if(existingCompound != null){
                            existingCompound.x = compoundWidget.getX();
                            existingCompound.y = compoundWidget.getY();
                            cNode.innerCompoundNodes.add(existingCompound);
                        }
                    }else{
                        cNode.innerNodes.add(new MagiculeCircuit.NodeData(
                                widget.getId(),
                                widget.getType(),
                                widget.getX(),
                                widget.getY()));

                        Object val = widget.getContentWidget() != null ? widget.getContentWidget().getCurrentValue() : null;
                        if(val != null){
                            cNode.innerNodeParameters.computeIfAbsent(widget.getId(), k -> new HashMap<>()).put("value", val);
                        }
                    }
                }

                for(MagiculeCircuit.WireData wire : this.circuit.getWires()){
                    cNode.innerWires.add(wire);
                }
                break;
            }

        }
    }

    private void updateBackButtonVisibility(){
        if(this.backButton != null){
            boolean inCompound = !layerStack.isEmpty();
            this.backButton.visible = inCompound;
            this.backButton.active = inCompound;
        }
    }

    public void copyNode(DraggableNodeWidget node){
        if(node.getContentWidget() != null){
            spawnNodeWithParam(node.getType(), node.getX() + 10, node.getY() + 10, node.getContentWidget().getCurrentValue());
        }else{
            spawnNode(node.getType(), node.getX() + 10, node.getY() + 10);
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

        for(DraggableNodeWidget node : nodeWidgets){
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
            for(DraggableNodeWidget node : nodeWidgets){
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
        saveCurrentTabCircuit();

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
        for (DraggableNodeWidget node : this.nodeWidgets) {
            if (node.getContentWidget() != null && node.keyPressed(event)) {
                return true;
            }
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(@NonNull CharacterEvent event) {
        for (DraggableNodeWidget node : this.nodeWidgets) {
            if (node.getContentWidget() != null && node.charTyped(event)) {
                return true;
            }
        }
        return super.charTyped(event);
    }

    //ノードの削除
    public void deleteNode(DraggableNodeWidget node){
        this.nodeWidgets.remove(node);
        this.removeWidget(node);
        this.circuit.removeNodeAndWires(node.getId());
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

        this.circuit.addNode(new MagiculeCircuit.NodeData(newId, type, (int) canvasX, (int) canvasY));
        this.circuit.setNodeParam(newId, "value", initialValue);

        DraggableNodeWidget newNode = new DraggableNodeWidget(this, newId, type, (int) canvasX, (int) canvasY, 80);
        this.nodeWidgets.add(newNode);
        addRenderableWidget(newNode);
        if(newNode.getContentWidget() != null){
            addRenderableWidget(newNode.getContentWidget());
        }
    }

    //画面上のノードリストからid指定に合致するものを返す
    public DraggableNodeWidget findNodeById(UUID id){
        for(DraggableNodeWidget node : this.nodeWidgets){
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
}
