package com.github.sweetfish111.reincarnated.player;

import net.minecraft.nbt.CompoundTag;

public interface PersistentComponent {
    void saveToNBT(CompoundTag tag);
    void loadFromNBT(CompoundTag tag);
}
