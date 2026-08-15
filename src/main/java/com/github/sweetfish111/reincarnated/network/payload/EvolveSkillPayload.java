package com.github.sweetfish111.reincarnated.network.payload;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record EvolveSkillPayload(String skillId) implements CustomPacketPayload {
    public static final Type<EvolveSkillPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("reincarnated", "evolve_skill"));
    public static final StreamCodec<ByteBuf, EvolveSkillPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, EvolveSkillPayload::skillId,
            EvolveSkillPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}