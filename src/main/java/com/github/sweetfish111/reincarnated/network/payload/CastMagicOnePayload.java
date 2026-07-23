package com.github.sweetfish111.reincarnated.network.payload;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CastMagicOnePayload() implements CustomPacketPayload {
   public static final Type<CastMagicOnePayload> TYPE = new Type(Identifier.fromNamespaceAndPath("reincarnated","cast_magic_1"));
   public static final StreamCodec<ByteBuf, CastMagicOnePayload> CODEC = StreamCodec.unit(new CastMagicOnePayload());

    public Type<? extends CustomPacketPayload> type(){
        return TYPE;
    }
}
