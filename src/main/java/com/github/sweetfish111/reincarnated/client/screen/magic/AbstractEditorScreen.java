package com.github.sweetfish111.reincarnated.client.screen.magic;

import com.github.sweetfish111.reincarnated.circuit.EditorTab;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractEditorScreen extends Screen {
    protected List<Button> tabBtns = new ArrayList<>();

    public AbstractEditorScreen(Component title) {
        super(title);
    }

    protected void initTabBtns(EditorTab currentTab){
        int startX = 10;
        for (EditorTab tab : EditorTab.values()) {
            Button tabBtn = Button.builder(
                    Component.literal(tab.getDisplayName()),
                    button -> onTabSelected(tab)
            ).bounds(startX, 5, 60, 20).build();
            tabBtn.active = (tab != currentTab);
            tabBtns.add(tabBtn);
            addRenderableWidget(tabBtn);
            startX += 60;
        }
    }

    protected abstract void onTabSelected(EditorTab tab);

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    public boolean clickTabButton(MouseButtonEvent event, boolean doubleClick) {
        for(Button btn : tabBtns){
            if(btn.mouseClicked(event,doubleClick)){
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }
}
