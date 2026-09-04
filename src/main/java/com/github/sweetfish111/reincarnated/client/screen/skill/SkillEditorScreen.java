package com.github.sweetfish111.reincarnated.client.screen.skill;

import com.github.sweetfish111.reincarnated.circuit.EditorTab;
import com.github.sweetfish111.reincarnated.client.screen.WorkspaceCamera;
import com.github.sweetfish111.reincarnated.client.screen.magic.AbstractEditorScreen;
import com.github.sweetfish111.reincarnated.reincarnated;
import com.github.sweetfish111.reincarnated.skill.SkillEffect;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SkillEditorScreen extends AbstractEditorScreen {
    private Identifier slotImage = Identifier.fromNamespaceAndPath(reincarnated.MODID, "textures/gui/container/skill_slot_1x1");
    private final WorkspaceCamera camera = new WorkspaceCamera();
    private SkillRank skillRank = SkillRank.UNAWAKENED;
    private final Set<SkillEffect> physicalSkills = new HashSet<>();
    private final Set<SkillEffect> soulSkills = new HashSet<>();

    public SkillEditorScreen(Set<SkillEffect> physicalSkill, Set<SkillEffect> soulSkill, SkillRank skillRank) {
        super(Component.literal("skill editor"));
        this.physicalSkills.addAll(physicalSkill);
        this.soulSkills.addAll(soulSkill);
        this.skillRank = skillRank;
        this.slotImage = skillRank.getSlotImage();
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

        graphics.blit(slotImage, 200, 100, 0, 0, 176.0f, 166.0f, 176.0f, 166.0f);

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
