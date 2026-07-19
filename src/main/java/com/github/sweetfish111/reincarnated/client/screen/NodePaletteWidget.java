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
    }

    public void openContextMenu(int sX, int sY, DraggableNodeWidget target){
        this.isOpen = true;
        this.menuHeight = calcMenuHeight(2);
        this.contextMenuTarget = target;
        this.screenX = sX;
        this.screenY = sY;
    }

    private int calcMenuHeight(int size){return size * ITEM_HEIGHT;}

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
                    int index = (int)((mouseY - screenY) / ITEM_HEIGHT);
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
                    int index = (int)((mouseY - screenY) / ITEM_HEIGHT);
                    if (index >= 0 && index < MagiculeNodeType.values().length){
                        MagiculeNodeType selectedType = MagiculeNodeType.values()[index];
                        this.paretScreen.spawnNode(selectedType, this.spawnCanvasX, this.spawnCanvasY);
                        return true;
                    }
                }
            }
        }

        this.isOpen = false;
        return false;
    }

    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY){
        if(!this.isOpen) return;

        guiGraphicsExtractor.fill(screenX, screenY, screenX + MENU_WIDTH, screenY + menuHeight, 0xDD000000);
        guiGraphicsExtractor.outline(screenX, screenY, MENU_WIDTH, menuHeight, 0xFFFFFFFF);

        if(contextMenuTarget != null){
            int i = 0;
            for(ContextMenuFacts fact : ContextMenuFacts.values()){
                int itemY = screenY + (i * ITEM_HEIGHT);
                int color = (mouseX >= screenX && mouseX <= screenX + MENU_WIDTH && mouseY >= itemY && mouseY <= itemY + ITEM_HEIGHT) ? 0xFFFFFF00 : 0xFFFFFFFF;

                guiGraphicsExtractor.centeredText(Minecraft.getInstance().font, fact.toString() , screenX + 30, itemY + ITEM_HEIGHT / 4, color);
                i++;
            }
        }else{
            int i = 0;
            for(MagiculeNodeType type : MagiculeNodeType.values()){
                int itemY = screenY + (i * ITEM_HEIGHT);
                //item上にカーソルがあるかによって色を変更
                int color = (mouseX >= screenX && mouseX <= screenX + MENU_WIDTH && mouseY >= itemY && mouseY <= itemY + ITEM_HEIGHT) ? 0xFFFFFF00 : 0xFFFFFFFF;

                guiGraphicsExtractor.centeredText(Minecraft.getInstance().font, type.displayName, screenX + 30, itemY + ITEM_HEIGHT / 4, color);
                i++;
            }
        }
    }
}
