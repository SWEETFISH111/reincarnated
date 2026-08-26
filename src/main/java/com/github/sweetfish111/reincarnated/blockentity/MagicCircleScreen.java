package com.github.sweetfish111.reincarnated.blockentity;

import com.github.sweetfish111.reincarnated.reincarnated;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class MagicCircleScreen extends AbstractContainerScreen<MagicCircleMenu> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(reincarnated.MODID, "textures/gui/container/magic_circle.png");

    public MagicCircleScreen(MagicCircleMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, x + this.imageWidth, y + this.imageHeight, 0.0f, 1.0f, 0.0f, 1.0f);
    }
}
