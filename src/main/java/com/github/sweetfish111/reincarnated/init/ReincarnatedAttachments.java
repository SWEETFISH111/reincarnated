package com.github.sweetfish111.reincarnated.init;

import com.github.sweetfish111.reincarnated.common.CommonData;
import com.github.sweetfish111.reincarnated.player.PhysicalData;
import com.github.sweetfish111.reincarnated.player.PlayerMagicData;
import com.github.sweetfish111.reincarnated.player.SoulData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ReincarnatedAttachments {
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

    public static final Supplier<AttachmentType<CommonData>> COMMON_DATA =
            ATTACHMENT_TYPES.register("commmon_data",
                    () -> AttachmentType.builder(CommonData::new)
                            .serialize(new IAttachmentSerializer<CommonData>() {
                                @Override
                                public CommonData read(IAttachmentHolder holder, ValueInput input) {
                                    CommonData data = new CommonData();
                                    input.read("common_data", CompoundTag.CODEC).ifPresent(data::loadFromNBT);
                                    return data;
                                }

                                @Override
                                public boolean write(CommonData attachment, ValueOutput output) {
                                    CompoundTag tag = attachment.saveToNBT();
                                    output.store("common_data", CompoundTag.CODEC, tag);
                                    return true;
                                }
                            })
                            .copyOnDeath()
                            .build()
            );

    public static final Supplier<AttachmentType<PhysicalData>> PHYSICAL_DATA =
            ATTACHMENT_TYPES.register("physical_data",
                    () -> AttachmentType.builder(PhysicalData::new)
                            .serialize(new IAttachmentSerializer<PhysicalData>() {
                                @Override
                                public PhysicalData read(IAttachmentHolder holder, ValueInput input) {
                                    PhysicalData data = new PhysicalData();
                                    input.read("physical_data", CompoundTag.CODEC).ifPresent(data::loadFromNBT);
                                    return data;
                                }

                                @Override
                                public boolean write(PhysicalData attachment, ValueOutput output) {
                                    CompoundTag tag = attachment.saveToNBT();
                                    output.store("physical_data", CompoundTag.CODEC, tag);
                                    return true;
                                }
                            })
                            .build()
            );

    public static final Supplier<AttachmentType<SoulData>> SOUL_DATA =
            ATTACHMENT_TYPES.register("soul_data",
                    () -> AttachmentType.builder(SoulData::new)
                            .serialize(new IAttachmentSerializer<SoulData>() {
                                @Override
                                public SoulData read(IAttachmentHolder holder, ValueInput input) {
                                    SoulData data = new SoulData();
                                    input.read("soul_data", CompoundTag.CODEC).ifPresent(data::loadFromNBT);
                                    return data;
                                }

                                @Override
                                public boolean write(SoulData attachment, ValueOutput output) {
                                    CompoundTag tag = attachment.saveToNBT();
                                    output.store("soul_data", CompoundTag.CODEC, tag);
                                    return true;
                                }
                            })
                            .copyOnDeath()
                            .build()
            );
}
