package com.github.sweetfish111.reincarnated.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RequestCircuitPayload() implements CustomPacketPayload {

    public static final Type<RequestCircuitPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("reincarnated","request_circuit"));
    public static final StreamCodec<ByteBuf, RequestCircuitPayload> CODEC = StreamCodec.unit(new RequestCircuitPayload());
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
