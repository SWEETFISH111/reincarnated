package com.github.sweetfish111.reincarnated.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SaveCircuitPayload(CompoundTag circuitData) implements CustomPacketPayload {
    public static final Type<SaveCircuitPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("magicmod", "save_circuit"));

    public static final StreamCodec<FriendlyByteBuf, SaveCircuitPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, // 「中身はNBTだよ」と教える
            SaveCircuitPayload::circuitData, // 送る時：箱からNBTを取り出す
            SaveCircuitPayload::new          // 受け取った時：新しい手紙として復元する
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {return TYPE;}
}
