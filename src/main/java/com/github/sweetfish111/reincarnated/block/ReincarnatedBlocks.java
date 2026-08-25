package com.github.sweetfish111.reincarnated.block;

import com.github.sweetfish111.reincarnated.reincarnated;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;


public class ReincarnatedBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(reincarnated.MODID);
    public static final DeferredBlock<Block> MAGIC_CIRCLE = BLOCKS.register(
            "magic_circle",
            registryName -> new MagicCircle(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .destroyTime(2.0f)
                    .explosionResistance(10.0f)
                    .sound(SoundType.GRAVEL)
                    .lightLevel(state -> 0)
                    .noOcclusion()  // ★追加：このブロックは隣接ブロックの面を隠さない、という宣言
            ));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
