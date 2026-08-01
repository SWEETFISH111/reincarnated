package com.github.sweetfish111.reincarnated.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record StopCastPayload()implements CustomPacketPayload {
    public static final Type<StopCastPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("reincarnated", "stop_cast"));
    public static final StreamCodec<FriendlyByteBuf, StopCastPayload>CODEC = StreamCodec.unit(new StopCastPayload());
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
