package com.github.sweetfish111.reincarnated.client.screen.skill;

import com.github.sweetfish111.reincarnated.reincarnated;
import com.github.sweetfish111.reincarnated.skill.SkillEffect;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SkillSlot extends AbstractWidget {
    private SkillRank currentSkillRank = SkillRank.UNAWAKENED;
    private Slot slot1 = new Slot(SkillEffect.SOUL_EATER);
    private Map<Slot[], Boolean> slot2 = Map.of(new Slot[8], false);
    private Map<Slot[], Boolean> slot3 = Map.of(new Slot[8], false);

    public SkillSlot(int x, int y, int width, int height, Component message, SkillRank currentSkillRank) {
        super(x, y, width, height, message);
        this.currentSkillRank = currentSkillRank;
    }

    public void setCurrentSkillRank(SkillRank skillRank){
        this.currentSkillRank = skillRank;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, float v) {
        guiGraphicsExtractor.blit(currentSkillRank.getSlotImage(), 200, 100, 0, 0, 176, 166, 176, 166);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        //todo ナレーションの作成
    }

    private class Slot{
        private SkillEffect subSkill = null;
        private boolean isLocked = false;

        public Slot(){}
        public Slot(SkillEffect subSkill){
            this.subSkill = subSkill;
            this.isLocked = true;
        }

        public void setSubSkill(SkillEffect subSkill){
            if(!isLocked){
                this.subSkill = subSkill;
            }
        }

        public void clearSkill(){
            subSkill = null;
        }
    }
}
