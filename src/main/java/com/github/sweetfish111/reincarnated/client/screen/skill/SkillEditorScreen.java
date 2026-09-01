package com.github.sweetfish111.reincarnated.client.screen.skill;

import com.github.sweetfish111.reincarnated.circuit.EditorTab;
import com.github.sweetfish111.reincarnated.client.screen.magic.AbstractEditorScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class SkillEditorScreen extends AbstractEditorScreen {
    public SkillEditorScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        super.initTabBtns(EditorTab.SKILL);

        super.init();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {

        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    protected void onTabSelected(EditorTab tab) {
        if(tab.equals(EditorTab.MAGIC)){

        }else if(tab.equals(EditorTab.ARTS)){

        }
    }
}
