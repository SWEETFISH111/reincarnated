package com.github.sweetfish111.reincarnated.item;

import com.github.sweetfish111.reincarnated.reincarnated;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.Consumable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

import static com.github.sweetfish111.reincarnated.block.ReincarnatedBlocks.MAGIC_CIRCLE;

public class ReincarnatedItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(reincarnated.MODID);

    // 2. グリモワール（魔導書）の登録
    public static final DeferredItem<Item> GRIMOIRE = ITEMS.registerItem(
            "grimoire",
            GrimoireItem::new, // コンストラクタ参照（またはラムダ式）
            props -> props.stacksTo(1)
    );
    public static final DeferredItem<Item> MASO_STONE = ITEMS.registerItem(
            "maso_stone",
            Item::new,
            props -> props.rarity(Rarity.UNCOMMON)
    );
    public static final DeferredItem<BlockItem> MAGIC_CIRCLE_ITEM = ITEMS.registerSimpleBlockItem("magic_circle", MAGIC_CIRCLE);

    // 3. メインのMODクラスから呼び出す初期化メソッド
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
