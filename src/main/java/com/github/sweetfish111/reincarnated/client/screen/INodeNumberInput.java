package com.github.sweetfish111.reincarnated.client.screen;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;

public interface INodeNumberInput {
    boolean handleCharTyped(CharacterEvent codePoint);
    boolean handleKeyPressed(KeyEvent scanCode);
    EditBox getEditBox();
}
