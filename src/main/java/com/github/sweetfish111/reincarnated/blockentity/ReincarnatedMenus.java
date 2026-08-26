package com.github.sweetfish111.reincarnated.blockentity;

import com.github.sweetfish111.reincarnated.reincarnated;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ReincarnatedMenus {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, reincarnated.MODID);

    public static final Supplier<MenuType<MagicCircleMenu>> MAGIC_CIRCLE_MENU = MENU_TYPES.register(
            "magic_circle_menu",
            () -> IMenuTypeExtension.create((windowId, inv, data) -> {
                var pos = data.readBlockPos();
                var container = new net.minecraft.world.SimpleContainer(5);
                return new MagicCircleMenu(windowId, inv, container);
            })
    );

    public static void register(IEventBus eventBus){
        MENU_TYPES.register(eventBus);
    }
}