package com.github.sweetfish111.reincarnated.network.payload;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SyncCircuitPayload(CompoundTag magicDataTag) implements CustomPacketPayload {
    public static final Type<SyncCircuitPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("reincarnated", "sync_circuit"));

    public static final StreamCodec<ByteBuf, SyncCircuitPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, SyncCircuitPayload::magicDataTag,
            SyncCircuitPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
