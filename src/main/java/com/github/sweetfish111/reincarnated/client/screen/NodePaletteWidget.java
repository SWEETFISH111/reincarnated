package com.github.sweetfish111.reincarnated.client.screen;

import com.github.sweetfish111.reincarnated.circuit.MagiculeNodeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.List;

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

    private List<MagiculeNodeType> getAvailableNodeTypes() {
        List<MagiculeNodeType> available = new ArrayList<>();
        MagicEditorScreen.EditorTab currentTab = paretScreen.getCurrentTab();

        for (MagiculeNodeType type : MagiculeNodeType.values()){
            if(type.isAvailableFor(currentTab)){
                available.add(type);
            }
        }
        return available;
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

        List<MagiculeNodeType> availableTypes = getAvailableNodeTypes();
        menuHeight = calcMenuHeight(availableTypes.size());

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
                    if (index >= 0 && index < availableTypes.size()){
                        MagiculeNodeType selectedType = availableTypes.get(index);
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

        List<MagiculeNodeType> types = getAvailableNodeTypes();
        int maxScroll = (contextMenuTarget != null) ? 0 : Math.max(0, types.size() - MAX_VISIBLE_ITEMS);

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

        List<MagiculeNodeType> availableTypes = getAvailableNodeTypes();
        int visibleCount = Math.min(
                (contextMenuTarget != null) ? ContextMenuFacts.values().length : availableTypes.size(),
                MAX_VISIBLE_ITEMS
        );
        if (contextMenuTarget != null) {
            ContextMenuFacts[] facts = ContextMenuFacts.values();
            menuHeight = calcMenuHeight(facts.length);

            guiGraphicsExtractor.fill(screenX, screenY, screenX + MENU_WIDTH, screenY + menuHeight, 0xDD000000);
            guiGraphicsExtractor.outline(screenX, screenY, MENU_WIDTH, menuHeight, 0xFFFFFFFF);
            guiGraphicsExtractor.enableScissor(screenX, screenY, screenX + MENU_WIDTH, screenY + menuHeight);

            for (int i = 0; i < visibleCount; i++) {
                int itemIndex = i + scrollOffset;
                if (itemIndex >= facts.length) break;

                int itemY = screenY + (i * ITEM_HEIGHT);
                int color = (mouseX >= screenX && mouseX <= screenX + MENU_WIDTH && mouseY >= itemY && mouseY <= itemY + ITEM_HEIGHT) ? 0xFFFFFF00 : 0xFFFFFFFF;

                guiGraphicsExtractor.centeredText(Minecraft.getInstance().font, facts[itemIndex].toString(), screenX + (MENU_WIDTH / 2), itemY + ITEM_HEIGHT / 4, color);
            }
        } else {

            guiGraphicsExtractor.fill(screenX, screenY, screenX + MENU_WIDTH, screenY + menuHeight, 0xDD000000);
            guiGraphicsExtractor.outline(screenX, screenY, MENU_WIDTH, menuHeight, 0xFFFFFFFF);
            guiGraphicsExtractor.enableScissor(screenX, screenY, screenX + MENU_WIDTH, screenY + menuHeight);

            for (int i = 0; i < visibleCount; i++) {
                // 2. スクロールオフセットを考慮したインデックス取得
                int itemIndex = i + scrollOffset;
                if (itemIndex >= availableTypes.size()) break;

                int itemY = screenY + (i * ITEM_HEIGHT);
                int color = (mouseX >= screenX && mouseX <= screenX + MENU_WIDTH && mouseY >= itemY && mouseY <= itemY + ITEM_HEIGHT) ? 0xFFFFFF00 : 0xFFFFFFFF;

                guiGraphicsExtractor.centeredText(Minecraft.getInstance().font, availableTypes.get(itemIndex).displayName, screenX + (MENU_WIDTH / 2), itemY + ITEM_HEIGHT / 4, color);
            }
        }

        if(availableTypes.size() > MAX_VISIBLE_ITEMS && contextMenuTarget == null){
            int barHeight = Math.max(4, (menuHeight * MAX_VISIBLE_ITEMS) / availableTypes.size());
            int maxScroll = availableTypes.size() - MAX_VISIBLE_ITEMS;
            int barY = screenY + (scrollOffset * (menuHeight - barHeight)) / maxScroll;

            // 右端に薄いバーを描画
            guiGraphicsExtractor.fill(screenX + MENU_WIDTH - 3, barY, screenX + MENU_WIDTH - 1, barY + barHeight, 0xAAAAAAAA);
        }
    }
}
