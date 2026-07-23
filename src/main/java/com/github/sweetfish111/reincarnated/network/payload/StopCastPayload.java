package com.github.sweetfish111.reincarnated.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record StopCastPayload(String triggerType)implements CustomPacketPayload {
    public static final Type<StopCastPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("reincarnated", "stop_cast"));
    public static final StreamCodec<FriendlyByteBuf, StopCastPayload>CODEC = CustomPacketPayload.codec(
            StopCastPayload::write,
            StopCastPayload::new
    );
    public StopCastPayload(FriendlyByteBuf buffer) {
        this(buffer.readUtf());
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.triggerType);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
