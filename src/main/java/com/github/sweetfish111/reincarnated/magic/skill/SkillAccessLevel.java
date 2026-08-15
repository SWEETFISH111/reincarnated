package com.github.sweetfish111.reincarnated.magic.skill;

public enum SkillAccessLevel {
    /** 1. 一切の操作拒否：存在のみ認知（または文字化け）、ダイブ不可、編集不可、発動不可 */
    DENIED(0, true, false, false),

    /** 2. 閲覧のみ：内部回路（ノード・ワイヤー）の参照・解析・ダイブ可能、発動可能、ただしノード追加・削除・変更不可 */
    READ_ONLY(1, true, true, false),

    /** 3. 編集可能：すべての操作（ノード追加・削除、ワイヤー配線、パラメータ変更、ダイブ）が許可 */
    EDITABLE(2, true, true, true);

    private final int level;
    private final boolean canExecute;
    private final boolean canViewInner;
    private final boolean canModify;

    SkillAccessLevel(int level, boolean canExecute, boolean canViewInner, boolean canModify) {
        this.level = level;
        this.canExecute = canExecute;
        this.canViewInner = canViewInner;
        this.canModify = canModify;
    }

    public int getLevel() { return level; }
    public boolean canExecute() { return canExecute; }
    public boolean canViewInner() { return canViewInner; }
    public boolean canModify() { return canModify; }

    public boolean isAtLeast(SkillAccessLevel required) {
        return this.level >= required.level;
    }

    public static SkillAccessLevel fromIndex(int index) {
        for (SkillAccessLevel val : values()) {
            if (val.level == index) return val;
        }
        return DENIED;
    }
}