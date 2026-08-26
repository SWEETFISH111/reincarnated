package com.github.sweetfish111.reincarnated.blockentity;

import com.github.sweetfish111.reincarnated.block.MagicCircle;
import com.github.sweetfish111.reincarnated.block.ReincarnatedBlocks;
import com.github.sweetfish111.reincarnated.reincarnated;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ReincarnatedBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, reincarnated.MODID);

    public static final Supplier<BlockEntityType<MagicCircleEntity>> MAGIC_CIRCLE_ENTITY = BLOCK_ENTITY_TYPES.register(
        "magic_circle_entity",
            () -> new BlockEntityType<>(
                    MagicCircleEntity::new,
                    false,
                    ReincarnatedBlocks.MAGIC_CIRCLE.get()
            )
    );

    public static void register(IEventBus eventBus){BLOCK_ENTITY_TYPES.register(eventBus);}
}
