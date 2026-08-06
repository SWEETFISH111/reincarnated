package com.github.sweetfish111.reincarnated.client.screen;

import com.github.sweetfish111.reincarnated.circuit.*;
import com.github.sweetfish111.reincarnated.network.payload.ExportSpellPalyload;
import com.github.sweetfish111.reincarnated.network.payload.SaveCircuitPayload;
import com.github.sweetfish111.reincarnated.player.PlayerMagicData;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.util.*;


public class MagicEditorScreen extends Screen {
    private ScreenLayerManager thisLayerManager = new ScreenLayerManager();
    private final List<AbstructDraggingNodeWidget> nodeWidgets = new ArrayList<>();

    private EditBox popupBox;

    private Button exportBtn;
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

    public ScreenLayerManager getThisLayerManager(){return this.thisLayerManager;}
    public void setCollapseTargets(List<UUID> collapseTargets){
        this.collapseTargets.clear();
        this.collapseTargets.addAll(collapseTargets);
    }

    //初期化
    @Override
    protected void init(){
        super.init();
        thisLayerManager.init(this.magicData);
        int startX = 10;
        for (EditorTab tab : EditorTab.values()) {
            Button tabBtn = Button.builder(
                    Component.literal(tab.getDisplayName()),
                    button -> switchTab(tab)
            ).bounds(startX, 5, 60, 20).build();

            startX += 60;

            tabBtn.active = (tab != thisLayerManager.getCurrentTab());
            thisLayerManager.getTabBtns().add(tabBtn);
            addRenderableWidget(tabBtn);
        }
        Button backBtn = Button.builder(
                Component.literal("<- 戻る"),
                button -> goBackLayer()
        ).bounds(this.width - 60 -20 -60, 5, 60, 20).build();
        thisLayerManager.setBackBtn(backBtn);
        addRenderableWidget(backBtn);

        thisLayerManager.updateBackButtonVisibility();
        this.addRenderableWidget(thisLayerManager.getBackBtn());

        // 画面の右上座標を計算 (例: 画面右端から少し内側、上部から少し下)
        int buttonWidth = 60;
        int buttonHeight = 20;
        int posX = this.width - buttonWidth - 10;
        int posY = 10;

        // プレイヤーが手に「白紙の本」を持っているかチェック
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            ItemStack heldItem = minecraft.player.getMainHandItem();

            if (heldItem.is(Items.BOOK)) {
                System.out.println("ugoiteruyo");
                // 右上に「エクスポート」ボタンを追加
                exportBtn = Button.builder(Component.literal("💾 焼き付け"), button -> {
                    // 現在のワークスペースの回路をNBT化
                    CompoundTag circuitTag = this.thisLayerManager.getWorkCircuit().saveToNBT();

                    // サーバーへパケット送信
                    ExportSpellPalyload payload = new ExportSpellPalyload(circuitTag);
                    if(net.minecraft.client.Minecraft.getInstance().getConnection() != null){
                        net.minecraft.client.Minecraft.getInstance().getConnection().send(payload);
                    }
                }).bounds(posX, posY, buttonWidth, buttonHeight).build();

                System.out.println(exportBtn.toString());
                this.addRenderableWidget(exportBtn);
            }
        }

        rebuildNodeWidgets();
    }

    public void switchTab(EditorTab tab){
        thisLayerManager.saveCurrentTabCircuit(this.nodeWidgets);
        thisLayerManager.switchTab(tab);
        clearCanvasWidgets();
        rebuildNodeWidgets();
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
        System.out.println("MagicEditorScreen_rebuild_start : " + thisLayerManager.getWorkCircuit());
        clearCanvasWidgets();
        palette.close();
        for(MagiculeCircuit.NodeData nodeData : thisLayerManager.getWorkCircuit().getNodes()){
            AbstructDraggingNodeWidget nodeWidget = new DraggableNodeWidget(
                    this,
                    nodeData.id,
                    nodeData.x,
                    nodeData.y,
                    80,
                    nodeData.type
            );
            this.nodeWidgets.add(nodeWidget);
            this.addRenderableWidget(nodeWidget);
            if(nodeWidget.getContentWidget() != null){
                this.addRenderableWidget(nodeWidget.getContentWidget().getContentWidget());
            }
        }
        for(MagiculeCircuit.CompoundNodeData compoundNodeData : thisLayerManager.getWorkCircuit().getCompoundNodes()){
            CompoundNodeWidget compoundWidget = new CompoundNodeWidget(
                    this,
                    compoundNodeData.id,
                    compoundNodeData.x,
                    compoundNodeData.y,
                    80,
                    compoundNodeData.customName
            );
            this.nodeWidgets.add(compoundWidget);
            this.addRenderableWidget(compoundWidget);
        }
        System.out.println("MagicEditorScreen_rebuild_End" + thisLayerManager.getWorkCircuit());
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
                        thisLayerManager.getWorkCircuit().addWire(sourceNode.getId(), sourcePort.getIndex(), targetNode.getId(),targetPort.getIndex(), sourcePort.getDataType() != PortDataType.EXEC && targetPort.getDataType() != PortDataType.EXEC);

                        return;
                    }
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.thisLayerManager.tick();
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
        for(MagiculeCircuit.WireData wire : thisLayerManager.getWorkCircuit().getWires()){
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



        String errorMsg = this.thisLayerManager.getErrorMessage();
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
        if(exportBtn != null){
            exportBtn.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTick);
        }
        for(Button btn : thisLayerManager.getTabBtns()){
            btn.extractRenderState(guiGraphicsExtractor, (int)canvasMouseX, (int)canvasMouseY, partialTick);
        }
        if(thisLayerManager.getBackBtn() != null){
            thisLayerManager.getBackBtn().extractRenderState(guiGraphicsExtractor, (int) canvasMouseX, (int) canvasMouseY, partialTick);
        }
        this.palette.render(guiGraphicsExtractor, mouseX, mouseY);

        if(this.isNamingCompound && this.popupBox != null){
            this.popupBox.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTick);
        }
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

        //タブボタンのクリック判定
        for(Button btn : thisLayerManager.getTabBtns()){
            if(btn.mouseClicked(event,doubleClick)){
                return true;
            }
        }

        if(thisLayerManager.getBackBtn().mouseClicked(event, doubleClick)){
            return true;
        }

        //exportBtnのクリック判定
        if(exportBtn != null && exportBtn.mouseClicked(event, doubleClick)){
            return true;
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
                    thisLayerManager.diveLayer(cNode, nodeWidgets);
                    rebuildNodeWidgets();
                    System.out.println("MagicEditorScreen : doubleClick -> CompoundNodewidget");
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
                        System.out.println("MagicEditorScreen : kurikkusita ->" + node);
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
        this.thisLayerManager.goBackLayer(this.nodeWidgets);
        rebuildNodeWidgets();
        LOGGER.info("hitotu ue no kaisou ni modorimasita");
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
                spawnNodeWithParam(MagiculeNodeType.COMPOUND, cNode.getX() + 10, cNode.getY() + 10, cNode.getContentWidget().getCurrentValue());
            }else{
                spawnNode(MagiculeNodeType.COMPOUND, cNode.getX() + 10, cNode.getY() + 10);
            }
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
        thisLayerManager.saveCurrentInnerCircuit(thisLayerManager.getLayerStack().peek(), this.nodeWidgets);
        thisLayerManager.saveCurrentTabCircuit(this.nodeWidgets);

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
                this.thisLayerManager.getWorkCircuit().collapseNodes(collapseTargets, customName);
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
        this.nodeWidgets.remove(node);
        this.removeWidget(node);
        thisLayerManager.getWorkCircuit().removeNodeAndWires(node.getId());
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

        thisLayerManager.getWorkCircuit().addNode(new MagiculeCircuit.NodeData(newId, type, (int) canvasX, (int) canvasY));
        thisLayerManager.getWorkCircuit().setNodeParam(newId, "value", initialValue);

        DraggableNodeWidget newNode = new DraggableNodeWidget(this, newId, (int) canvasX, (int) canvasY, 80, type);
        this.nodeWidgets.add(newNode);
        addRenderableWidget(newNode);
        if(newNode.getContentWidget() != null){
            addRenderableWidget(newNode.getContentWidget());
        }
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
                Component.literal("カスタムネーム入力")
        );
        this.popupBox.setMaxLength(15);
        this.popupBox.setValue("MyCustomMagic");

        this.setFocused(this.popupBox);
        this.popupBox.setFocused(true);

        this.addRenderableWidget(this.popupBox);
    }
}
