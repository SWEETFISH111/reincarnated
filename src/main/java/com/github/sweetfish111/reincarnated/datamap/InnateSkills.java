package com.github.sweetfish111.reincarnated.datamap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.github.sweetfish111.reincarnated.skill.SkillEffect;

import java.util.List;

public record InnateSkills(List<SkillEffect> skills) {
    public static final Codec<InnateSkills> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    SkillEffect.CODEC.listOf().fieldOf("skills").forGetter(InnateSkills::skills)
            ).apply(instance, InnateSkills::new));
}