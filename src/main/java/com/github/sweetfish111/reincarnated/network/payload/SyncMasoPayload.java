package com.github.sweetfish111.reincarnated.network.payload;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SyncMasoPayload(float maxMaso, float currentMaso, float maxBarrier, float currentBarrier) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncMasoPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("reincarnated", "maso_data"));

    public static final StreamCodec<ByteBuf, SyncMasoPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            SyncMasoPayload::maxMaso,
            ByteBufCodecs.FLOAT,
            SyncMasoPayload::currentMaso,
            ByteBufCodecs.FLOAT,
            SyncMasoPayload::maxBarrier,
            ByteBufCodecs.FLOAT,
            SyncMasoPayload::currentBarrier,
            SyncMasoPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
