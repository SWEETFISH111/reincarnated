package com.github.sweetfish111.reincarnated.network.payload;

import com.mojang.datafixers.types.Type;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.apache.commons.compress.archivers.dump.DumpArchiveEntry;

public record SlotEnablePayload(boolean isEnable) implements CustomPacketPayload {
    public static final Type<SlotEnablePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("reincarnated", "slot_enable"));

    public static final StreamCodec<ByteBuf, SlotEnablePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            SlotEnablePayload::isEnable,
            SlotEnablePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
