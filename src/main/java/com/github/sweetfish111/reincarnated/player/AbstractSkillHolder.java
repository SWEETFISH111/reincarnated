package com.github.sweetfish111.reincarnated.player;

import com.github.sweetfish111.reincarnated.skill.SkillEffect;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public abstract class AbstractSkillHolder {

    private final Set<SkillEffect> ownedSkillEffects = EnumSet.noneOf(SkillEffect.class);
    private final Set<SkillEffect> activeSkillEffects = EnumSet.noneOf(SkillEffect.class);

    public void addOwnedSkillEffect(SkillEffect effect) {
        ownedSkillEffects.add(effect);
    }

    public boolean hasOwnedSkillEffect(SkillEffect effect) {
        return ownedSkillEffects.contains(effect);
    }

    public Set<SkillEffect> getOwnedSkillEffects() {
        return Collections.unmodifiableSet(ownedSkillEffects);
    }

    public boolean activateSkillEffect(SkillEffect effect) {
        if (!ownedSkillEffects.contains(effect)) return false;
        activeSkillEffects.add(effect);
        return true;
    }

    public void deactivateSkillEffect(SkillEffect effect) {
        activeSkillEffects.remove(effect);
    }

    public boolean isSkillEffectActive(SkillEffect effect) {
        return activeSkillEffects.contains(effect);
    }

    public Set<SkillEffect> getActiveSkillEffects() {
        return Collections.unmodifiableSet(activeSkillEffects);
    }

    public CompoundTag saveToNBT() {
        CompoundTag rootTag = new CompoundTag();

        ListTag ownedTag = new ListTag();
        for (SkillEffect effect : ownedSkillEffects) {
            ownedTag.add(StringTag.valueOf(effect.getSerializedName()));
        }
        rootTag.put("owned", ownedTag);

        ListTag activeTag = new ListTag();
        for (SkillEffect effect : activeSkillEffects) {
            activeTag.add(StringTag.valueOf(effect.getSerializedName()));
        }
        rootTag.put("active", activeTag);

        return rootTag;
    }

    public void loadFromNBT(CompoundTag rootTag) {
        if (rootTag == null || rootTag.isEmpty()) return;

        ListTag ownedTag = rootTag.getListOrEmpty("owned");
        for (int i = 0; i < ownedTag.size(); i++) {
            findByName(ownedTag.getString(i).get()).ifPresent(ownedSkillEffects::add);
        }

        ListTag activeTag = rootTag.getListOrEmpty("active");
        for (int i = 0; i < activeTag.size(); i++) {
            findByName(activeTag.getString(i).get()).ifPresent(activeSkillEffects::add);
        }
    }

    private static Optional<SkillEffect> findByName(String name) {
        return Arrays.stream(SkillEffect.values())
                .filter(e -> e.getSerializedName().equals(name))
                .findFirst();
    }
}