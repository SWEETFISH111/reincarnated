package com.github.sweetfish111.reincarnated.skill;

import net.minecraft.util.StringRepresentable;
import com.mojang.serialization.Codec;

public enum SkillEffect implements StringRepresentable {
    SLOW_FALL("slow_fall", 1, SkillCategory.MOVEMENT),
    UNDERWATER_BREATHING("underwater_breathing", 1, SkillCategory.UTILITY),
    FLAME_RESISTANCE("underwater_resistance", 1, SkillCategory.RESISTANCE),
    FLIGHT("flight", 1, SkillCategory.MOVEMENT),
    GRAVITY_MANIPULATION("gravity_manipulation", 1, SkillCategory.MANIPULATION)
    ;

    public static final Codec<SkillEffect> CODEC = StringRepresentable.fromEnum(SkillEffect::values);

    private final String id;
    private final int boxCost;
    private final SkillCategory category;

    SkillEffect(String id, int boxCost, SkillCategory category) {
        this.id = id;
        this.boxCost = boxCost;
        this.category = category;
    }

    @Override
    public String getSerializedName() {
        return id;
    }

    public int boxCost() {
        return boxCost;
    }

    public SkillCategory category() {
        return category;
    }
}