package com.github.sweetfish111.reincarnated.circuit;

public enum EditorTab {
    MAGIC("魔法"),
    SKILL("スキル"),
    ARTS("アーツ");

    private final String displayName;

    EditorTab(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}