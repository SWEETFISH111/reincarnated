package com.github.sweetfish111.reincarnated.network.payload;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

public record SyncStatusPayload(
        String masoStageName,
        float currentMaso,
        float maxMaso,
        float masoRegenRate,
        double masoStylePreference,

        float barrierPoint,
        float maxBarrierPoint,
        float barrierDamageReduction,
        double barrierStylePreference,

        String currentUniqueSkill,
        boolean completeGreedy,
        double greedyScore,
        double predatorScore,
        double scavengerScore,
        double hoarderScore,
        double usurperScore,
        List<String> evolvableUniqueSkills
) implements CustomPacketPayload {

    public static final Type<SyncStatusPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("reincarnated", "sync_status"));

    private static final StreamCodec<ByteBuf, List<String>> STRING_LIST_CODEC =
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list());

    public static final StreamCodec<ByteBuf, SyncStatusPayload> STREAM_CODEC = StreamCodec.of(
            (ByteBuf buf, SyncStatusPayload payload) -> {
                ByteBufCodecs.STRING_UTF8.encode(buf, payload.masoStageName());
                ByteBufCodecs.FLOAT.encode(buf, payload.currentMaso());
                ByteBufCodecs.FLOAT.encode(buf, payload.maxMaso());
                ByteBufCodecs.FLOAT.encode(buf, payload.masoRegenRate());
                ByteBufCodecs.DOUBLE.encode(buf, payload.masoStylePreference());

                ByteBufCodecs.FLOAT.encode(buf, payload.barrierPoint());
                ByteBufCodecs.FLOAT.encode(buf, payload.maxBarrierPoint());
                ByteBufCodecs.FLOAT.encode(buf, payload.barrierDamageReduction());
                ByteBufCodecs.DOUBLE.encode(buf, payload.barrierStylePreference());

                ByteBufCodecs.STRING_UTF8.encode(buf, payload.currentUniqueSkill());
                ByteBufCodecs.BOOL.encode(buf, payload.completeGreedy());
                ByteBufCodecs.DOUBLE.encode(buf, payload.greedyScore());
                ByteBufCodecs.DOUBLE.encode(buf, payload.predatorScore());
                ByteBufCodecs.DOUBLE.encode(buf, payload.scavengerScore());
                ByteBufCodecs.DOUBLE.encode(buf, payload.hoarderScore());
                ByteBufCodecs.DOUBLE.encode(buf, payload.usurperScore());
                STRING_LIST_CODEC.encode(buf, payload.evolvableUniqueSkills());
            },
            (ByteBuf buf) -> {
                String masoStageName = ByteBufCodecs.STRING_UTF8.decode(buf);
                float currentMaso = ByteBufCodecs.FLOAT.decode(buf);
                float maxMaso = ByteBufCodecs.FLOAT.decode(buf);
                float masoRegenRate = ByteBufCodecs.FLOAT.decode(buf);
                double masoStylePreference = ByteBufCodecs.DOUBLE.decode(buf);

                float barrierPoint = ByteBufCodecs.FLOAT.decode(buf);
                float maxBarrierPoint = ByteBufCodecs.FLOAT.decode(buf);
                float barrierDamageReduction = ByteBufCodecs.FLOAT.decode(buf);
                double barrierStylePreference = ByteBufCodecs.DOUBLE.decode(buf);

                String currentUniqueSkill = ByteBufCodecs.STRING_UTF8.decode(buf);
                boolean completeGreedy = ByteBufCodecs.BOOL.decode(buf);
                double greedyScore = ByteBufCodecs.DOUBLE.decode(buf);
                double predatorScore = ByteBufCodecs.DOUBLE.decode(buf);
                double scavengerScore = ByteBufCodecs.DOUBLE.decode(buf);
                double hoarderScore = ByteBufCodecs.DOUBLE.decode(buf);
                double usurperScore = ByteBufCodecs.DOUBLE.decode(buf);
                List<String> evolvableUniqueSkills = STRING_LIST_CODEC.decode(buf);

                return new SyncStatusPayload(
                        masoStageName, currentMaso, maxMaso, masoRegenRate, masoStylePreference,
                        barrierPoint, maxBarrierPoint, barrierDamageReduction, barrierStylePreference,
                        currentUniqueSkill, completeGreedy, greedyScore, predatorScore,
                        scavengerScore, hoarderScore, usurperScore, evolvableUniqueSkills
                );
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}