package com.github.sweetfish111.reincarnated.common;

import net.minecraft.nbt.CompoundTag;

public class CommonData {
    private String boxName;

    public boolean hasNamedBox() {
        return boxName != null && !boxName.isEmpty();
    }

    public String getBoxName() { return boxName; }
    public void setBoxName(String name) { this.boxName = name; }

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