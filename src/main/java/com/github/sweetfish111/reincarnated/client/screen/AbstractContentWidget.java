package com.github.sweetfish111.reincarnated.client.screen;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

public abstract class AbstractContentWidget extends AbstractWidget implements IContentWidget {

    public AbstractContentWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }
}
