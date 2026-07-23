package com.github.sweetfish111.reincarnated.init;

import com.github.sweetfish111.reincarnated.circuit.PlayerMagicData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, "reincarnated");

    public static final Supplier<AttachmentType<PlayerMagicData>> PLAYER_MAGIC_DATA =
            ATTACHMENT_TYPES.register("player_magic_data",
                    () -> AttachmentType.builder(PlayerMagicData::new)
                            .serialize(new IAttachmentSerializer<PlayerMagicData>() {
                                @Override
                                public boolean write(PlayerMagicData attachment, ValueOutput output) {
                                    // PlayerMagicData から CompoundTag を取得
                                    CompoundTag tag = attachment.saveToNBT();

                                    output.store("magic_data", CompoundTag.CODEC, tag);
                                    return true; // 書き込み対象が不正な場合
                                }

                                @Override
                                public PlayerMagicData read(IAttachmentHolder holder, ValueInput input) {
                                    PlayerMagicData data = new PlayerMagicData();

                                    // ValueInput から CompoundTag を読み込んで復元
                                    input.read("magic_data", CompoundTag.CODEC).ifPresent(data::loadFromNBT);
                                    return data;
                                }
                            })
                            .copyOnDeath()
                            .build());
}
