package com.github.sweetfish111.reincarnated.client.screen;

import com.github.sweetfish111.reincarnated.circuit.*;
import com.github.sweetfish111.reincarnated.network.payload.SaveCircuitPayload;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
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
    private final List<Button> tabButtons = new ArrayList<>();

    private final WorkspaceCamera camera = new WorkspaceCamera();
    private final NodePaletteWidget palette = new NodePaletteWidget(this);

    private DraggableNodeWidget activeNode = null;

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
        rebuildNodeWidgets();
    }

    private void switchTab(EditorTab tab){
        if(this.currentTab == tab)return;
        saveCurrentTabCircuit();

        this.currentTab = tab;

        for(int i = 0; i < EditorTab.values().length; i++){
            if(i < this.tabButtons.size()){
                this.tabButtons.get(i).active = (EditorTab.values()[i] != this.currentTab);
            }
        }
        clearCanvasWidgets();
        loadTabCircuit(this.currentTab);
    }

    private void clearCanvasWidgets(){
        for(DraggableNodeWidget nodeWidget : this.nodeWidgets){
            if(nodeWidget.getContentWidget() != null){
                this.removeWidget(nodeWidget.getContentWidget().getContentWidget());
            }
            this.removeWidget(nodeWidget);
        }
        this.nodeWidgets.clear();
    }

    private void saveCurrentTabCircuit(){
        this.circuit.getNodes().clear();
        for(DraggableNodeWidget widget : this.nodeWidgets){
            this.circuit.addNode(new MagiculeCircuit.NodeData(
                    widget.getId(),
                    widget.getType(),
                    widget.getX(),
                    widget.getY()
            ));
        }
        this.magicData.setCircuits(this.currentTab, this.circuit);
    }

    private void loadTabCircuit(EditorTab tab){
        this.circuit = this.magicData.getCircuit(tab);
        rebuildNodeWidgets();
    }

    private void rebuildNodeWidgets(){
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
        //選択中のノード
        this.activeNode = null;

        for(Button btn : this.tabButtons){
            if(btn.mouseClicked(event,doubleClick)){
                return true;
            }
        }
        //パレットのクリック判定
        if(palette.isOpen()){
            palette.mouseClicked(rawX, rawY, event.button());
        }
        //ノードのクリック＆右クリック判定
        for(DraggableNodeWidget node : this.nodeWidgets){

            if(node.handleCanvasClick(event, canvasX, canvasY, event.button())){
                this.activeNode = node;
                if(event.button() == 1){
                    if(node.portClicked((int)canvasX, (int)canvasY, event.button())){
                        return true;
                    }
                    palette.openContextMenu((int)rawX, (int)rawY, node);
                    return true;
                }
                return true;
            }
        }

        //何もない空間の右クリック
        if(event.button() == 1){
            this.palette.openPalette((int)rawX, (int)rawY, canvasX, canvasY);
            return true;
        }

        return this.camera.mouseClicked(event.x(), event.y(), event.button());
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

        if(this.activeNode != null){
            return this.activeNode.handleCanvasDragged(canvasX, canvasY, canvasDragX, canvasDragY);
        }
        return this.camera.mouseDragged(dragX,dragY);
    }

    //マウスが離されたら初期状態に戻る
    @Override
    public boolean mouseReleased(MouseButtonEvent event){
        double canvasX = this.camera.getCanvasX(event.x());
        double canvasY = this.camera.getCanvasY(event.y());
        if(this.activeNode != null){
            this.activeNode.handleCanvasReleased(canvasX, canvasY, event.button());
            this.activeNode = null;
            return true;
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
