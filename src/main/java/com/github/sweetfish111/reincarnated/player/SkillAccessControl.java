package com.github.sweetfish111.reincarnated.player;

import com.github.sweetfish111.reincarnated.magic.skill.SkillAccessLevel;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;

/**
 * スキルCompoundNode回路（②：進化した公式回路そのもの）の閲覧/編集権限を管理する。
 * 将来的にアクセス制御全般（回路単位の権限、共有時の権限等）を引き受ける置き場として独立させてある。
 */
public class SkillAccessControl implements PersistentComponent {
    private final Map<String, SkillAccessLevel> skillPermissions = new HashMap<>();

    public SkillAccessLevel getSkillAccessLevel(String skillId) {
        return skillPermissions.getOrDefault(skillId, SkillAccessLevel.DENIED);
    }

    public void setSkillAccessLevel(String skillId, SkillAccessLevel level) {
        skillPermissions.put(skillId, level);
    }

    @Override
    public void saveToNBT(CompoundTag tag) {
        skillPermissions.forEach((skillId, level) -> tag.putInt(skillId, level.getLevel()));
    }

    @Override
    public void loadFromNBT(CompoundTag tag) {
        skillPermissions.clear();
        for (String skillId : tag.keySet()) {
            skillPermissions.put(skillId, SkillAccessLevel.fromIndex(tag.getInt(skillId).orElse(0)));
        }
    }
}