package com.github.sweetfish111.reincarnated.network.payload;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SelectMagicSlotPayload(int slotIndex) implements CustomPacketPayload {
    public static final Type<SelectMagicSlotPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("reincarnated", "select_magic_slot"));

    public static final StreamCodec<ByteBuf, SelectMagicSlotPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SelectMagicSlotPayload::slotIndex,
            SelectMagicSlotPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}