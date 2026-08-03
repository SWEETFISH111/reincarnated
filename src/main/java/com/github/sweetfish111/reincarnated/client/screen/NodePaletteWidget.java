package com.github.sweetfish111.reincarnated.client.screen;

import com.github.sweetfish111.reincarnated.circuit.EditorTab;
import com.github.sweetfish111.reincarnated.circuit.MagiculeNodeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.*;

public class NodePaletteWidget {

    public static class PaletteItem {
        private final Component displayName;
        private final Runnable onClickAction;

        public PaletteItem(Component displayName, Runnable onClickAction) {
            this.displayName = displayName;
            this.onClickAction = onClickAction;
        }

        public Component getDisplayName() { return displayName; }
        public void execute() { onClickAction.run(); }
    }

    private final MagicEditorScreen parentScreen;
    private boolean isOpen = false;
    private int screenX = 0;
    private int screenY = 0;
    private double spawnCanvasX = 0;
    private double spawnCanvasY = 0;

    private Set<AbstructDraggingNodeWidget> contextMenuTargets = new HashSet<>();
    private static final int MENU_WIDTH = 100;
    private static final int ITEM_HEIGHT = 20;
    private int menuHeight;

    private static final int MAX_VISIBLE_ITEMS = 6;
    private int scrollOffset = 0;

    private List<PaletteItem> paletteItems = new ArrayList<>();

    // コンストラクタ
    public NodePaletteWidget(MagicEditorScreen parentScreen){
        this.parentScreen = parentScreen;
    }

    public void openPalette(int sX, int sY, double cX, double cY) {
        this.isOpen = true;
        this.contextMenuTargets.clear();
        this.screenX = sX;
        this.screenY = sY;
        this.spawnCanvasX = cX;
        this.spawnCanvasY = cY;
        this.scrollOffset = 0;
        this.paletteItems = createPaletteFactory();z
    }

    public void openContextMenu(int sX, int sY, Set<AbstructDraggingNodeWidget> target){
        this.isOpen = true;
        this.contextMenuTargets = target != null ? target : new HashSet<>();
        this.screenX = sX;
        this.screenY = sY;
        this.scrollOffset = 0;
        this.paletteItems = createPaletteFactory();
    }

    private List<PaletteItem> createPaletteFactory(){
        List<PaletteItem> items = new ArrayList<>();

        if(!contextMenuTargets.isEmpty()){
            items.add(new PaletteItem(Component.literal("Delete"), () -> {
                for (AbstructDraggingNodeWidget node : contextMenuTargets) {
                    this.parentScreen.deleteNode(node);
                }
            }));

            items.add(new PaletteItem(Component.literal("Copy"), () -> {
                for (AbstructDraggingNodeWidget node : contextMenuTargets) {
                    this.parentScreen.copyNode(node);
                }
            }));

            items.add(new PaletteItem(Component.literal("Collapse"), () -> {
                this.parentScreen.openCompoundNamingPopup();
                List<UUID> targetNodes = new ArrayList<>();
                for (AbstructDraggingNodeWidget node : this.contextMenuTargets) {
                    targetNodes.add(node.getId());
                }
                this.parentScreen.setCollapseTargets(targetNodes);
            }));

            boolean hasCompound = contextMenuTargets.stream().anyMatch(n -> n instanceof CompoundNodeWidget);
            if (hasCompound) {
                items.add(new PaletteItem(Component.literal("Open"), () -> {
                    for (AbstructDraggingNodeWidget node : contextMenuTargets) {
                        if (node instanceof CompoundNodeWidget cNode) {
                            cNode.openContents();
                        }
                    }
                }));
            }
        } else {
            EditorTab currentTab = parentScreen.getThisLayerManager().getCurrentTab();
            for (MagiculeNodeType type : MagiculeNodeType.values()) {
                if (type.isAvailableFor(currentTab)) {
                    items.add(new PaletteItem(Component.literal(type.displayName), () -> {
                        this.parentScreen.spawnNode(type, this.spawnCanvasX, this.spawnCanvasY);
                    }));
                }
            }
        }
        return items;
    }

    private int calcMenuHeight(int size){
        return Math.min(size, MAX_VISIBLE_ITEMS) * ITEM_HEIGHT;
    }

    public boolean isOpen(){
        return this.isOpen;
    }

    public void close(){
        this.isOpen = false;
        for(AbstructDraggingNodeWidget active : contextMenuTargets){
            active.setFocused(false);
        }
        this.contextMenuTargets.clear();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isOpen) return false;

        int visibleCount = Math.min(paletteItems.size(), MAX_VISIBLE_ITEMS);
        menuHeight = calcMenuHeight(visibleCount);

        if (button == 0) {
            if (mouseX >= screenX && mouseX <= screenX + MENU_WIDTH && mouseY >= screenY && mouseY <= screenY + menuHeight) {
                int clickedIndex = (int) ((mouseY - screenY) / ITEM_HEIGHT) + scrollOffset;
                if (clickedIndex >= 0 && clickedIndex < paletteItems.size()) {
                    paletteItems.get(clickedIndex).execute();
                    close();
                    return true;
                }
            }
        }

        this.isOpen = false;
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta){
        if (!isOpen) return false;

        int maxScroll = Math.max(0, paletteItems.size() - MAX_VISIBLE_ITEMS);

        if (delta > 0) {
            this.scrollOffset = Math.max(0, this.scrollOffset - 1);
        } else if (delta < 0) {
            this.scrollOffset = Math.min(maxScroll, this.scrollOffset + 1);
        }
        return true;
    }

    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY) {
        if (!this.isOpen) return;

        int visibleCount = Math.min(paletteItems.size(), MAX_VISIBLE_ITEMS);
        menuHeight = calcMenuHeight(visibleCount);

        guiGraphicsExtractor.fill(screenX, screenY, screenX + MENU_WIDTH, screenY + menuHeight, 0xDD000000);
        guiGraphicsExtractor.outline(screenX, screenY, MENU_WIDTH, menuHeight, 0xFFFFFFFF);
        guiGraphicsExtractor.enableScissor(screenX, screenY, screenX + MENU_WIDTH, screenY + menuHeight);

        for (int i = 0; i < visibleCount; i++) {
            int itemIndex = i + scrollOffset;
            if (itemIndex >= paletteItems.size()) break;

            int itemY = screenY + (i * ITEM_HEIGHT);
            int color = (mouseX >= screenX && mouseX <= screenX + MENU_WIDTH && mouseY >= itemY && mouseY <= itemY + ITEM_HEIGHT) ? 0xFFFFFF00 : 0xFFFFFFFF;

            guiGraphicsExtractor.centeredText(Minecraft.getInstance().font, paletteItems.get(itemIndex).getDisplayName(), screenX + (MENU_WIDTH / 2), itemY + ITEM_HEIGHT / 4, color);
        }

        guiGraphicsExtractor.disableScissor();

        if (paletteItems.size() > MAX_VISIBLE_ITEMS) {
            int barHeight = Math.max(4, (menuHeight * MAX_VISIBLE_ITEMS) / paletteItems.size());
            int maxScroll = paletteItems.size() - MAX_VISIBLE_ITEMS;
            int barY = screenY + (scrollOffset * (menuHeight - barHeight)) / maxScroll;

            guiGraphicsExtractor.fill(screenX + MENU_WIDTH - 3, barY, screenX + MENU_WIDTH - 1, barY + barHeight, 0xAAAAAAAA);
        }
    }
}