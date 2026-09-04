package com.github.sweetfish111.reincarnated.client.screen.skill;

import com.github.sweetfish111.reincarnated.reincarnated;
import net.minecraft.resources.Identifier;

public enum SkillRank{
    UNAWAKENED(Identifier.fromNamespaceAndPath(reincarnated.MODID, "textures/gui/container/skill_slot_1x1")),
    UNIQUE(Identifier.fromNamespaceAndPath(reincarnated.MODID, "textures/gui/container/skill_slot_3x3")),
    ULTIMATE(Identifier.fromNamespaceAndPath(reincarnated.MODID, "textures/gui/container/skill_slot_5x5"));

    private final Identifier slot;

    SkillRank(Identifier slot) {
        this.slot = slot;
    }

    public Identifier getSlotImage(){return this.slot;}
}