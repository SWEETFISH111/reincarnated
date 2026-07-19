package com.github.sweetfish111.reincarnated.client.screen;

import com.github.sweetfish111.reincarnated.circuit.ContentWidgetType;
import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.circuit.MagiculeNodeType;
import com.github.sweetfish111.reincarnated.circuit.PortDataType;
import com.github.sweetfish111.reincarnated.network.SaveCircuitPayload;
import com.github.sweetfish111.reincarnated.circuit.ContentWidgetType;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MagicEditorScreen extends Screen {

    private final MagiculeCircuit circuit = new MagiculeCircuit();

    private final List<DraggableNodeWidget> nodeWidgets = new ArrayList<>();

    private final WorkspaceCamera camera = new WorkspaceCamera();
    private final NodePaletteWidget palette = new NodePaletteWidget(this);

    private DraggableNodeWidget activeNode = null;

    private DraggableNodeWidget contextMenuTarget = null;

    private static final Logger LOGGER = LogUtils.getLogger();

    //コンストラクタ
    public MagicEditorScreen() {super(Component.literal("魔法編集"));}
    public MagicEditorScreen(CompoundTag initialData){
        this();
        LOGGER.info("画面に届いたデータ" + (initialData != null ? initialData.toString() : "null"));
        if(initialData != null && !initialData.isEmpty()){
            this.circuit.loadFromNBT(initialData);
            LOGGER.info("サーバーから届いたデータをキャンバスに復元した！");
        }else{
            LOGGER.info("データが空だったので、真っ白なキャンバスを用意した！");
        }

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
                if(nodeWidget.getContentWidget() instanceof INodeNumberInput){
                    INodeNumberInput numInput = (INodeNumberInput) nodeWidget.getContentWidget();
                    this.addRenderableWidget(numInput.getEditBox());
                }
            }
        }
    }

    //ゲッター
    public List<DraggableNodeWidget> getNodeWidgets(){return nodeWidgets;}
    public MagiculeCircuit getCircuit(){return this.circuit;}
    public NodePaletteWidget getPalette(){return this.palette;}

    //初期化
    @Override
    protected void init(){
        super.init();
    }

    //ポートにつながって存在が確定したワイヤーを記録する
    public void onWireDropped(DraggableNodeWidget sourceNode, NodePort sourcePort, double dropX, double dropY){
        if(sourcePort.getType() != NodePort.Type.OUTPUT) return;

        for(DraggableNodeWidget targetNode : this.nodeWidgets){
            if(targetNode != sourceNode){
                for(NodePort targetPort : targetNode.getInputPorts()){
                    if(targetPort.isMouseOver(dropX, dropY)){
                        if(sourcePort.getDataType() != targetPort.getDataType()){
                            LOGGER.info("ポートの型が違うため接続できません");
                            return;
                        }
                        if(sourcePort.getDataType() != PortDataType.EXEC){
                            this.circuit.addWire(sourceNode.getId(), sourcePort.getIndex(), targetNode.getId(),targetPort.getIndex(),true);
                        }else{
                            this.circuit.addWire(sourceNode.getId(), sourcePort.getIndex(), targetNode.getId(),targetPort.getIndex(),false);
                        }

                        LOGGER.info("繋がった" + sourceNode.getType() + "->" + targetNode.getType());
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
            if(source != null && target != null){
                NodePort outPort = source.getOutputPorts().get(wire.sourcePortIndex);
                NodePort inPort = target.getInputPorts().get(wire.targetPortIndex);
                boolean isDataFlow = (outPort.getDataType() != PortDataType.EXEC);
                drawMagiculeWire(guiGraphicsExtractor, outPort.getX() + 3, outPort.getY() + 3, inPort.getX() + 3, inPort.getY() + 3, isDataFlow);
            }
        }
        //ノード描画
        for (net.minecraft.client.gui.components.Renderable renderable : this.renderables){
            renderable.extractRenderState(guiGraphicsExtractor, (int)canvasMouseX, (int)canvasMouseY, partialTick);
        }
        guiGraphicsExtractor.pose().popMatrix();
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
        //パレットのクリック判定
        if(palette.isOpen()){
            palette.mouseClicked(rawX, rawY, event.button());
        }
        //ノードのクリック＆右クリック判定
        for(DraggableNodeWidget node : this.nodeWidgets){

            if(node.handleCanvasClick(event, canvasX, canvasY, event.button())){
                this.activeNode = node;
                if(node.getContentWidget() instanceof NumberInputContentWidget numWidget){
                    numWidget.mouseClicked(event, false);
                }
                if(event.button() == 1){
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
            spawnNodeWithParam(node.getType(), node.getX() + 10, node.getY() + 10, node.getContentWidget().getCurrentvalue());
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
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        this.camera.zoomAt(x, y, scrollY);
        return true;
    }

    //魔法編集画面全体を閉じたとき
    @Override
    public void onClose() {

        this.circuit.getNodes().clear();

        for(DraggableNodeWidget widget : this.nodeWidgets){
            this.circuit.addNode(new MagiculeCircuit.NodeData(
                    widget.getId(),
                    widget.getType(),
                    widget.getX(),
                    widget.getY()
            ));
        }
        LOGGER.info("===サーバーへのpayloadの送信===");
        net.minecraft.nbt.CompoundTag savedData = this.circuit.saveToNBT();
        SaveCircuitPayload payload = new SaveCircuitPayload(savedData);
        if(net.minecraft.client.Minecraft.getInstance().getConnection() != null){
            net.minecraft.client.Minecraft.getInstance().getConnection().send(payload);
        }

        LOGGER.info("手紙の投函完了。NBTデータ:" + savedData.toString());
        super.onClose();
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent event) {
        for (DraggableNodeWidget node : this.nodeWidgets) {
            if (node.getContentWidget() != null && node.keyPressed(event)) {
                return true;
            }
        }
        if(event.hasControlDown() && event.key() == 86){
            double mx = Minecraft.getInstance().mouseHandler.xpos() * (double)Minecraft.getInstance().getWindow().getGuiScaledWidth() / (double)Minecraft.getInstance().getWindow().getWidth();
            double my = Minecraft.getInstance().mouseHandler.ypos() * (double)Minecraft.getInstance().getWindow().getGuiScaledHeight() / (double)Minecraft.getInstance().getWindow().getHeight();
            double canvasX = this.camera.getCanvasX(mx);
            double canvasY = this.camera.getCanvasY(my);

            spawnNodeWithParam(palette.getContextMenuTarget().getType(), canvasX, canvasY, contextMenuTarget.getContentWidget().getCurrentvalue());
            return true;
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
        if(node.getContentWidget() != null && node.getContentWidget() instanceof INodeNumberInput){
            INodeNumberInput numberInput = (INodeNumberInput) node.getContentWidget();
            this.removeWidget(numberInput.getEditBox());
        }
        this.removeWidget(node.getContentWidget());
        LOGGER.info("削除しました" + node.getType().displayName);
    }

    //画面に新しいノードを誕生させる
    public void spawnNode(MagiculeNodeType type, double canvasX, double canvasY){
        spawnNodeWithParam(type, canvasX, canvasY, 0.0);
    }
    public void spawnNodeWithParam(MagiculeNodeType type, double canvasX, double canvasY, double initialValue) {
        UUID newId = UUID.randomUUID();

        this.circuit.addNode(new MagiculeCircuit.NodeData(newId, type, (int) canvasX, (int) canvasY));
        this.circuit.setNodeParam(newId, "value", initialValue);

        DraggableNodeWidget newNode = new DraggableNodeWidget(this, newId, type, (int) canvasX, (int) canvasY, 80);
        this.nodeWidgets.add(newNode);
        this.addRenderableWidget(newNode);
        if (newNode.getContentWidget() != null) {
            if (newNode.getContentWidget() instanceof NumberInputContentWidget numWidget) {
                this.addRenderableWidget(numWidget.getEditBox());
            } else {
                this.addRenderableWidget(newNode.getContentWidget());
            }
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
