package com.github.sweetfish111.reincarnated.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SwitchTabToSkillPayload() implements CustomPacketPayload {
    public static final Type<SwitchTabToSkillPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("reincarnated", "switcch_tab_to_skill"));
    public static final StreamCodec<FriendlyByteBuf, SwitchTabToSkillPayload> CODEC = StreamCodec.unit(new SwitchTabToSkillPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
