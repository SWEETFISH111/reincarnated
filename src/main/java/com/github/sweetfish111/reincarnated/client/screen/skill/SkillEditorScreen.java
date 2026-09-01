package com.github.sweetfish111.reincarnated.client.screen.skill;

import com.github.sweetfish111.reincarnated.circuit.EditorTab;
import com.github.sweetfish111.reincarnated.client.screen.WorkspaceCamera;
import com.github.sweetfish111.reincarnated.client.screen.magic.AbstractEditorScreen;
import com.github.sweetfish111.reincarnated.reincarnated;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class SkillEditorScreen extends AbstractEditorScreen {
    private Identifier TEXTURE = Identifier.fromNamespaceAndPath(reincarnated.MODID, "textures/gui/container/スキルスロット1x1");
    private WorkspaceCamera camera = new WorkspaceCamera();

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
        graphics.pose().pushMatrix();
        graphics.pose().translate((float)this.camera.panX, (float)this.camera.panY);
        graphics.pose().scale(this.camera.zoom, this.camera.zoom);

        graphics.blit(TEXTURE, 200, 100, 0, 0, 176, 166, 176, 166);

        graphics.pose().popMatrix();
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
