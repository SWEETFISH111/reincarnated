package com.github.sweetfish111.reincarnated.common;

import net.minecraft.nbt.CompoundTag;

public class CommonData {
    private final int CURRENT_DATA_VERSION = 1;


    public CompoundTag saveToNBT(){
        CompoundTag rootTag = new CompoundTag();
        rootTag.putInt("data_version", CURRENT_DATA_VERSION);
        return rootTag;
    }

    public void loadFromNBT(CompoundTag rootTag){
        if(rootTag == null || rootTag.isEmpty()) return;

        int version = rootTag.getIntOr("data_version", 0);


        if(version < 1) migrateV0toV1();
    }

    private void migrateV0toV1(){
        //何もしない
    }
}
