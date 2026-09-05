package com.github.sweetfish111.reincarnated.client.screen.skill;

import com.github.sweetfish111.reincarnated.skill.SkillEffect;
import com.mojang.serialization.Dynamic;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.Set;

public class SkillBox{
    private SkillRank currentSkillRank;
    private String boxName = "unwakened skill";
    private SkillSlot slot1 = new SkillSlot(SkillEffect.SOUL_EATER);
    private Map<SkillSlot[], Boolean> slot2 = Map.of(new SkillSlot[8], false);
    private Map<SkillSlot[], Boolean> slot3 = Map.of(new SkillSlot[8], false);

    public SkillBox(SkillRank currentSkillRank) {
        this.currentSkillRank = currentSkillRank;
    }

    public Map<SkillSlot[], Boolean> getSlot3() {
        return slot3;
    }

    public void setSlot3(Map<SkillSlot[], Boolean> slot3) {
        this.slot3 = slot3;
    }

    public Map<SkillSlot[], Boolean> getSlot2() {
        return slot2;
    }

    public void setSlot2(Map<SkillSlot[], Boolean> slot2) {
        this.slot2 = slot2;
    }

    public SkillSlot getSlot1() {
        return slot1;
    }

    public void setSlot1(SkillSlot slot1) {
        this.slot1 = slot1;
    }

    public SkillRank getCurrentSkillRank() {
        return currentSkillRank;
    }

    public void setCurrentSkillRank(SkillRank currentSkillRank) {
        this.currentSkillRank = currentSkillRank;
    }

    public String getBoxName() {
        return boxName;
    }

    public void setBoxName(String boxName) {
        this.boxName = boxName;
    }

    public CompoundTag saveToNBT(){
        CompoundTag rootTag = new CompoundTag();

        rootTag.putString("box_name", boxName);

        rootTag.putString("skill_rank", currentSkillRank.getSerializedName());

        if(slot1 == null){
            slot1 = new SkillSlot();
        }
        rootTag.put("slot_1", slot1.saveToNBT());

        ListTag uniqueSkillSlot = new ListTag();
        for(Map.Entry<SkillSlot[], Boolean> entry : slot2.entrySet()){
            for(SkillSlot slot : entry.getKey()){
                if(slot != null) {
                    uniqueSkillSlot.add(slot.saveToNBT());
                }else{
                    uniqueSkillSlot.add(new SkillSlot().saveToNBT());
                }
            }
        }
        rootTag.put("slot_2", uniqueSkillSlot);

        ListTag ultimateSkillSlot = new ListTag();
        for(Map.Entry<SkillSlot[], Boolean> entry : slot3.entrySet()){
            for(SkillSlot slot : entry.getKey()){
                if(slot != null){
                    ultimateSkillSlot.add(slot.saveToNBT());
                }
            }
        }
        rootTag.put("slot_3", ultimateSkillSlot);

        return rootTag;
    }

    public void loadFromNBT(CompoundTag rootTag){
        if(rootTag.contains("box_name")){
            boxName = rootTag.getStringOr("box_name", "unwakened skill");
        }
        if(rootTag.contains("skill_rank")){
            currentSkillRank = SkillRank.byName(rootTag.getStringOr("skill_rank", "slow_fall"));
        }
        if(rootTag.contains("slot_1")){
            slot1.loadFromNBT(rootTag.getCompoundOrEmpty("slot_1"));
        }
        if(rootTag.contains("slot_2")){
            ListTag listTag = new ListTag();
            listTag = rootTag.getListOrEmpty("slot_2");
            for(Map.Entry<SkillSlot[], Boolean> entry : slot2.entrySet()) {
                for (int i = 0; i < listTag.size(); i++) {
                    CompoundTag tag = listTag.getCompoundOrEmpty(i);
                    SkillSlot[] slot = entry.getKey();
                    if(slot[i] != null){
                        slot[i].loadFromNBT(tag);
                    }else{
                        slot[i] = new SkillSlot();
                    }
                }
            }
        }
        if(rootTag.contains("slot_3")){
            ListTag listTag = new ListTag();
            listTag = rootTag.getListOrEmpty("slot_3");
            for(Map.Entry<SkillSlot[], Boolean> entry : slot3.entrySet()) {
                for (int i = 0; i < listTag.size(); i++) {
                    CompoundTag tag = listTag.getCompoundOrEmpty(i);
                    SkillSlot[] slot = entry.getKey();
                    if(slot[i] != null){
                        slot[i].loadFromNBT(tag);
                    }else{
                        slot[i] = new SkillSlot();
                    }
                }
            }
        }
    }

    private class SkillSlot {
        private SkillEffect subSkill;
        private boolean isLocked = false;

        public SkillSlot(){}
        public SkillSlot(SkillEffect subSkill){
            this.subSkill = subSkill;
            this.isLocked = true;
        }

        public void setSubSkill(SkillEffect subSkill){
            if(!isLocked){
                this.subSkill = subSkill;
            }
        }

        public void clearSubSkill(){
            subSkill = null;
        }

        public CompoundTag saveToNBT(){
            CompoundTag rootTag = new CompoundTag();
            if(subSkill != null){
                rootTag.putString("name", subSkill.getSerializedName());
            }
            rootTag.putBoolean("is_locked", isLocked);
            return rootTag;
        }

        public void loadFromNBT(CompoundTag rootTag){
            if(rootTag.contains("name")){
                String serializedName = rootTag.getStringOr("name", "slow_fall");
                this.subSkill = SkillEffect.byName(serializedName);
            }
        }
    }

}
