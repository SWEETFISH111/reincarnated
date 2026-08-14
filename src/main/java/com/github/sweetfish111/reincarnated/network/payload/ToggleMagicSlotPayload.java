package com.github.sweetfish111.reincarnated.network.payload;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ToggleMagicSlotPayload(int slotIndex, boolean enabled) implements CustomPacketPayload {
    public static final Type<ToggleMagicSlotPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("reincarnated", "toggle_magic_slot"));

    public static final StreamCodec<ByteBuf, ToggleMagicSlotPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ToggleMagicSlotPayload::slotIndex,
            ByteBufCodecs.BOOL, ToggleMagicSlotPayload::enabled,
            ToggleMagicSlotPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}