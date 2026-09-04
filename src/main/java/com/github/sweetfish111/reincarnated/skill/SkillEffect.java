package com.github.sweetfish111.reincarnated.skill;

import net.minecraft.util.StringRepresentable;
import com.mojang.serialization.Codec;

public enum SkillEffect implements StringRepresentable {
    SLOW_FALL("slow_fall", 1, SkillDomain.PHYSICAL),
    UNDERWATER_BREATHING("underwater_breathing", 1, SkillDomain.PHYSICAL),
    FLAME_RESISTANCE("flame_resistance", 1, SkillDomain.PHYSICAL),
    FLIGHT("flight", 1, SkillDomain.PHYSICAL),
    GRAVITY_MANIPULATION("gravity_manipulation", 1, SkillDomain.SOUL),
    SOUL_EATER("soul_eater", 1, SkillDomain.SOUL)
    ;

    public static final Codec<SkillEffect> CODEC = StringRepresentable.fromEnum(SkillEffect::values);

    private final String id;
    private final int boxCost;
    private final SkillDomain domain;

    SkillEffect(String id, int boxCost, SkillDomain category) {
        this.id = id;
        this.boxCost = boxCost;
        this.domain = category;
    }

    @Override
    public String getSerializedName() {
        return id;
    }

    public SkillDomain getDomain(){return this.domain;};

    public int boxCost() {
        return boxCost;
    }

}