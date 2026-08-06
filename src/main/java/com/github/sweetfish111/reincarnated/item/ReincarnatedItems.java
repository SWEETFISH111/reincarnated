package com.github.sweetfish111.reincarnated.item;

import com.github.sweetfish111.reincarnated.reincarnated;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class ReincarnatedItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(reincarnated.MODID);

    // 2. グリモワール（魔導書）の登録
    public static final DeferredItem<Item> GRIMOIRE = ITEMS.registerItem(
            "grimoire",
            GrimoireItem::new, // コンストラクタ参照（またはラムダ式）
            props -> props.stacksTo(1)
    );

    // 3. メインのMODクラスから呼び出す初期化メソッド
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
