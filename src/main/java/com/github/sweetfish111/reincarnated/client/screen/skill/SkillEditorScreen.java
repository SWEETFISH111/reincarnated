package com.github.sweetfish111.reincarnated.client.screen.skill;

import com.github.sweetfish111.reincarnated.circuit.EditorTab;
import com.github.sweetfish111.reincarnated.client.screen.WorkspaceCamera;
import com.github.sweetfish111.reincarnated.client.screen.magic.AbstractEditorScreen;
import com.github.sweetfish111.reincarnated.skill.SkillEffect;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.HashSet;
import java.util.Set;

public class SkillEditorScreen extends AbstractEditorScreen {
    private final WorkspaceCamera camera = new WorkspaceCamera();
    private Set<SkillEffect> physicalOwnedSkill = new HashSet<>();
    private Set<SkillEffect> soulOwnedSkill = new HashSet<>();
    private SkillBox skillBox;

    public SkillEditorScreen(Set<SkillEffect> physicalOwnedSkill, Set<SkillEffect> soulOwnedSkill, SkillBox skillBox) {
        super(Component.literal("skill editor"));
        this.physicalOwnedSkill.addAll(physicalOwnedSkill);
        this.soulOwnedSkill.addAll(soulOwnedSkill);
        this.skillBox = skillBox;
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

        graphics.blit(skillBox.getCurrentSkillRank().getSlotImage(), 200, 100, 0, 0, 176.0f, 166.0f, 176.0f, 166.0f);

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
