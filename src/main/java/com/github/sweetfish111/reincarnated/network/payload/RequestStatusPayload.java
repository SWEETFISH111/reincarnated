package com.github.sweetfish111.reincarnated.network.payload;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RequestStatusPayload() implements CustomPacketPayload {
    public static final Type<RequestStatusPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("reincarnated", "request_status"));
    public static final StreamCodec<ByteBuf, RequestStatusPayload> CODEC = StreamCodec.unit(new RequestStatusPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}