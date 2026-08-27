package com.github.sweetfish111.reincarnated.blockentity;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MagicCircleMenu extends AbstractContainerMenu {
    private final Container blockInventory;

    public MagicCircleMenu(int containerId, Inventory playerInventory, Container blockInventory){
        super(ReincarnatedMenus.MAGIC_CIRCLE_MENU.get(), containerId);
        this.blockInventory = blockInventory;

        // 魔法陣側：5枠を横一列に並べる（0=本, 1=魔石, 2〜4=拾得物）
        addGrid(blockInventory, 0, 8, 17, 2);
        addGrid(blockInventory, 2, 8 + 36 + 8, 17, 3);

        // プレイヤー側：メインインベントリ3行×9列
        addPlayerMainInventory(playerInventory, 8, 84);

        // プレイヤー側：ホットバー1行×9列
        addPlayerHotbar(playerInventory, 8, 142);
    }

    private void addGrid(Container container, int startIndex, int left, int top, int line) {
        for (int x = 0; x < line; ++x) {
            this.addSlot(new Slot(container, startIndex + x, left + x * 18, top));
        }
    }

    private void addPlayerMainInventory(Inventory playerInventory, int left, int top) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                int index = col + row * 9 + 9; // Inventory内では0〜8がホットバー、9〜35がメイン
                this.addSlot(new Slot(playerInventory, index, left + col * 18, top + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory, int left, int top) {
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, left + col * 18, top));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}