package com.github.sweetfish111.reincarnated.player;

import com.github.sweetfish111.reincarnated.skill.SkillDomain;
import com.github.sweetfish111.reincarnated.skill.SkillEffect;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import java.util.*;

public abstract class AbstractSkillHolder {

    private final Set<SkillEffect> ownedSkillEffects = EnumSet.noneOf(SkillEffect.class);
    private final Set<SkillEffect> activeSkillEffects = EnumSet.noneOf(SkillEffect.class);

    private final Map<SkillEffect, Integer> masteryProgress = new EnumMap<>(SkillEffect.class);

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

    public void removeOwnedSkillEffect(SkillEffect effect){
        ownedSkillEffects.remove(effect);
        activeSkillEffects.remove(effect);
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



    public int incrementMastery(SkillEffect effect) {
        return masteryProgress.merge(effect, 1, Integer::sum);
    }

    public int getMastery(SkillEffect effect) {
        return masteryProgress.getOrDefault(effect, 0);
    }

    public void clearMastery(SkillEffect effect) {
        masteryProgress.remove(effect);
    }

    public CompoundTag saveToNBT() {
        CompoundTag rootTag = new CompoundTag();

        //ownedSkillEffects
        ListTag ownedTag = new ListTag();
        for (SkillEffect effect : ownedSkillEffects) {
            ownedTag.add(StringTag.valueOf(effect.getSerializedName()));
        }
        rootTag.put("owned", ownedTag);

        //activeSkillEffects
        ListTag activeTag = new ListTag();
        for (SkillEffect effect : activeSkillEffects) {
            activeTag.add(StringTag.valueOf(effect.getSerializedName()));
        }
        rootTag.put("active", activeTag);

        //skillmastery
        CompoundTag masteryTag = new CompoundTag();
        masteryProgress.forEach((effect, count) -> masteryTag.putInt(effect.getSerializedName(), count));
        rootTag.put("mastery", masteryTag);

        return rootTag;
    }

    public void loadFromNBT(CompoundTag rootTag) {
        if (rootTag == null || rootTag.isEmpty()) return;

        //ownedSkillEffects
        ListTag ownedTag = rootTag.getListOrEmpty("owned");
        for (int i = 0; i < ownedTag.size(); i++) {
            findByName(ownedTag.getString(i).get()).ifPresent(ownedSkillEffects::add);
        }

        //activeSkillEffects
        ListTag activeTag = rootTag.getListOrEmpty("active");
        for (int i = 0; i < activeTag.size(); i++) {
            findByName(activeTag.getString(i).get()).ifPresent(activeSkillEffects::add);
        }

        //skillmastery
        CompoundTag masteryTag = rootTag.getCompoundOrEmpty("mastery");
        for (SkillEffect effect : SkillEffect.values()) {
            if (masteryTag.contains(effect.getSerializedName())) {
                masteryProgress.put(effect, masteryTag.getIntOr(effect.getSerializedName(), 0));
            }
        }
    }

    private static Optional<SkillEffect> findByName(String name) {
        return Arrays.stream(SkillEffect.values())
                .filter(e -> e.getSerializedName().equals(name))
                .findFirst();
    }
}