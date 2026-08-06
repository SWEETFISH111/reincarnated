package com.github.sweetfish111.reincarnated.network.payload;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ExportSpellPalyload(CompoundTag circuitTag)implements CustomPacketPayload {
    public static final Type<ExportSpellPalyload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("reincarnated", "export_spell"));

    public static final StreamCodec<ByteBuf, ExportSpellPalyload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG,
            ExportSpellPalyload::circuitTag,
            ExportSpellPalyload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

