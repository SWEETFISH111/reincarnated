package com.github.sweetfish111.reincarnated.commondata;

import com.github.sweetfish111.reincarnated.client.screen.skill.SkillRank;
import net.minecraft.nbt.CompoundTag;

public class CommonData {
    private String boxName;
    private SkillRank skillRank = SkillRank.UNAWAKENED;

    public boolean hasNamedBox() {
        return boxName != null && !boxName.isEmpty();
    }

    public String getBoxName() { return boxName; }
    public void setBoxName(String name) { this.boxName = name; }
    public SkillRank getSkillRank(){return skillRank;}
    public void setSkillRank(SkillRank skillRank){this.skillRank = skillRank;}

    public CompoundTag saveToNBT() {
        CompoundTag tag = new CompoundTag();
        if (boxName != null) tag.putString("box_name", boxName);
        return tag;
    }

    public void loadFromNBT(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) return;
        if (tag.contains("box_name")) {
            boxName = tag.getStringOr("box_name", "");
        }
    }
}