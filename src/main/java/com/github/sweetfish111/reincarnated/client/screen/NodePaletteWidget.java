package com.github.sweetfish111.reincarnated.client.screen;

import com.github.sweetfish111.reincarnated.circuit.MagiculeNodeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class NodePaletteWidget {
    private enum ContextMenuFacts{DELETE, COPY};
    private final MagicEditorScreen paretScreen;
    private boolean isOpen = false;
    private int screenX = 0;
    private int screenY = 0;
    private double spawnCanvasX = 0;
    private double spawnCanvasY = 0;

    private DraggableNodeWidget contextMenuTarget = null;
    private static final int MENU_WIDTH = 100;
    private static final int ITEM_HEIGHT = 20;
    private int menuHeight;

    private static final int MAX_VISIBLE_ITEMS = 6;
    private int scrollOffset = 0;

    //コンストラクタ
    public NodePaletteWidget(MagicEditorScreen parentScreen){
        this.paretScreen = parentScreen;
    }

    public void openPalette(int sX, int sY, double cX, double cY) {
        this.isOpen = true;
        this.menuHeight = calcMenuHeight(MagiculeNodeType.values().length);
        this.contextMenuTarget = null;
        this.screenX = sX;
        this.screenY = sY;
        this.spawnCanvasX = cX;
        this.spawnCanvasY = cY;
        this.scrollOffset = 0;
    }

    public void openContextMenu(int sX, int sY, DraggableNodeWidget target){
        this.isOpen = true;
        this.menuHeight = calcMenuHeight(2);
        this.contextMenuTarget = target;
        this.screenX = sX;
        this.screenY = sY;
        this.scrollOffset = 0;
    }

    private int calcMenuHeight(int size){return Math.min(size, MAX_VISIBLE_ITEMS) * ITEM_HEIGHT;}

    //げったー
    public boolean isOpen(){
        return this.isOpen;
    }
    public DraggableNodeWidget getContextMenuTarget(){return this.contextMenuTarget;}

    public void setContextMenuTarget(DraggableNodeWidget node){this.contextMenuTarget = node;}

    public void close(){
        this.isOpen = false;
        this.contextMenuTarget = null;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button){
        if(!this.isOpen) return false;
        if(button == 0){
            if(this.contextMenuTarget != null){
                if(mouseX >= screenX && mouseX <= screenX + MENU_WIDTH && mouseY >= screenY && mouseY <= screenY + menuHeight){
                    int index = (int)((mouseY - screenY) / ITEM_HEIGHT + scrollOffset);
                    if(index == 0){
                        this.paretScreen.deleteNode(this.contextMenuTarget);
                        close();
                        return true;
                    }else if(index == 1){
                        this.paretScreen.copyNode(this.contextMenuTarget);
                        close();
                        return true;
                    }
                }
            }else{
                if (mouseX >= screenX && mouseX <= screenX + MENU_WIDTH && mouseY >= screenY && mouseY <= screenY + menuHeight){
                    int index = (int)((mouseY - screenY) / ITEM_HEIGHT + scrollOffset);
                    if (index >= 0 && index < MagiculeNodeType.values().length){
                        MagiculeNodeType selectedType = MagiculeNodeType.values()[index];
                        this.paretScreen.spawnNode(selectedType, this.spawnCanvasX, this.spawnCanvasY);
                        close();
                        return true;
                    }
                }
            }
        }

        this.isOpen = false;
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta){
        if (!isOpen) return false;

        MagiculeNodeType[] types = MagiculeNodeType.values();
        int maxScroll = Math.max(0, types.length - MAX_VISIBLE_ITEMS);

        // delta > 0 は上スクロール、delta < 0 は下スクロール
        if (delta > 0) {
            this.scrollOffset = Math.max(0, this.scrollOffset - 1);
        } else if (delta < 0) {
            this.scrollOffset = Math.min(maxScroll, this.scrollOffset + 1);
        }
        return true;
    }

    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY){
        if(!this.isOpen) return;

        guiGraphicsExtractor.fill(screenX, screenY, screenX + MENU_WIDTH, screenY + menuHeight, 0xDD000000);
        guiGraphicsExtractor.outline(screenX, screenY, MENU_WIDTH, menuHeight, 0xFFFFFFFF);

        guiGraphicsExtractor.enableScissor(screenX, screenY, screenX + MENU_WIDTH, screenY + menuHeight);

        int visibleCount = Math.min(
                (contextMenuTarget != null) ? ContextMenuFacts.values().length : MagiculeNodeType.values().length,
                MAX_VISIBLE_ITEMS
        );

        if (contextMenuTarget != null) {
            ContextMenuFacts[] facts = ContextMenuFacts.values();
            for (int i = 0; i < visibleCount; i++) {
                int itemIndex = i + scrollOffset;
                if (itemIndex >= facts.length) break;

                int itemY = screenY + (i * ITEM_HEIGHT);
                int color = (mouseX >= screenX && mouseX <= screenX + MENU_WIDTH && mouseY >= itemY && mouseY <= itemY + ITEM_HEIGHT) ? 0xFFFFFF00 : 0xFFFFFFFF;

                guiGraphicsExtractor.centeredText(Minecraft.getInstance().font, facts[itemIndex].toString(), screenX + (MENU_WIDTH / 2), itemY + ITEM_HEIGHT / 4, color);
            }
        } else {
            MagiculeNodeType[] types = MagiculeNodeType.values();
            for (int i = 0; i < visibleCount; i++) {
                // 2. スクロールオフセットを考慮したインデックス取得
                int itemIndex = i + scrollOffset;
                if (itemIndex >= types.length) break;

                int itemY = screenY + (i * ITEM_HEIGHT);
                int color = (mouseX >= screenX && mouseX <= screenX + MENU_WIDTH && mouseY >= itemY && mouseY <= itemY + ITEM_HEIGHT) ? 0xFFFFFF00 : 0xFFFFFFFF;

                guiGraphicsExtractor.centeredText(Minecraft.getInstance().font, types[itemIndex].displayName, screenX + (MENU_WIDTH / 2), itemY + ITEM_HEIGHT / 4, color);
            }
        }

        if(MagiculeNodeType.values().length > MAX_VISIBLE_ITEMS){
            int barHeight = Math.max(4, (menuHeight * MAX_VISIBLE_ITEMS) / MagiculeNodeType.values().length);
            int maxScroll = MagiculeNodeType.values().length - MAX_VISIBLE_ITEMS;
            int barY = screenY + (scrollOffset * (menuHeight - barHeight)) / maxScroll;

            // 右端に薄いバーを描画
            guiGraphicsExtractor.fill(screenX + MENU_WIDTH - 3, barY, screenX + MENU_WIDTH - 1, barY + barHeight, 0xAAAAAAAA);
        }
    }
}
