package com.github.sweetfish111.reincarnated.player;

import com.github.sweetfish111.reincarnated.skill.SkillEffect;
import net.minecraft.nbt.CompoundTag;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class PhysicalData extends AbstractSkillHolder {
    private final int DATA_VERSION = 1;


    @Override
    public CompoundTag saveToNBT(){
        CompoundTag rootTag = new CompoundTag();
        rootTag.put("skills", super.saveToNBT());
        rootTag.putInt("data_version", DATA_VERSION);
        return rootTag;
    }

    @Override
    public void loadFromNBT(CompoundTag rootTag){
        if(rootTag == null || rootTag.isEmpty()) return;
        if(rootTag.contains("skills")){
            super.loadFromNBT(rootTag.getCompoundOrEmpty("skills"));
        }
        int version = rootTag.getIntOr("data_version", 0);
        if(version < 1) migrateV0toV1();
    }

    private void migrateV0toV1(){
        //何もしない
    }
}
