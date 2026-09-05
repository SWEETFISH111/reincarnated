package com.github.sweetfish111.reincarnated.client.screen.skill;

import com.github.sweetfish111.reincarnated.reincarnated;
import net.minecraft.resources.Identifier;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum SkillRank{
    UNAWAKENED("unwakened", Identifier.fromNamespaceAndPath(reincarnated.MODID, "textures/gui/container/skill_slot_1x1.png")),
    UNIQUE("unique", Identifier.fromNamespaceAndPath(reincarnated.MODID, "textures/gui/container/skill_slot_3x3.png")),
    ULTIMATE("ultimate", Identifier.fromNamespaceAndPath(reincarnated.MODID, "textures/gui/container/skill_slot_5x5.png"));

    private final Identifier slot;
    private final String id;
    private static final Map<String, SkillRank> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(SkillRank::getSerializedName, e -> e));

    SkillRank(String id, Identifier slot) {
        this.id = id;
        this.slot = slot;
    }

    public String getSerializedName(){
        return this.id;
    }

    public Identifier getSlotImage(){return this.slot;}

    public static SkillRank byName(String name){
        return BY_NAME.get(name);
    }
}