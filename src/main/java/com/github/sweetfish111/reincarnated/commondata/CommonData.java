package com.github.sweetfish111.reincarnated.commondata;

import com.github.sweetfish111.reincarnated.client.screen.skill.SkillBox;
import com.github.sweetfish111.reincarnated.client.screen.skill.SkillRank;
import net.minecraft.nbt.CompoundTag;

public class CommonData {
    private SkillBox skillBox = new SkillBox(SkillRank.UNAWAKENED);

    public SkillBox getSkillBox() {
        return skillBox;
    }

    public void setSkillBox(SkillBox skillBox) {
        this.skillBox = skillBox;
    }

    public CompoundTag saveToNBT() {
        CompoundTag rootTag = new CompoundTag();
        rootTag.put("skill_box", skillBox.saveToNBT());
        return rootTag;
    }

    public void loadFromNBT(CompoundTag rootTag) {
        if (rootTag == null || rootTag.isEmpty()) return;
        if(rootTag.contains("skill_box")){
            skillBox.loadFromNBT(rootTag.getCompoundOrEmpty("skill_box"));
        }
    }
}