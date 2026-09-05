package com.github.sweetfish111.reincarnated.network.payload;

import com.github.sweetfish111.reincarnated.client.screen.skill.SkillBox;
import com.github.sweetfish111.reincarnated.commondata.PhysicalData;
import com.github.sweetfish111.reincarnated.reincarnated;
import com.github.sweetfish111.reincarnated.skill.SkillEffect;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.HashSet;
import java.util.Set;

public record SyncSkillPayload(Set<String> physicalSkills, Set<String> soulSkills, CompoundTag skillBox) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncSkillPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(reincarnated.MODID, "sync_skill"));
    public static final StreamCodec<ByteBuf, SyncSkillPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.collection(HashSet::new)),
            SyncSkillPayload::physicalSkills,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.collection(HashSet::new)),
            SyncSkillPayload::soulSkills,
            ByteBufCodecs.COMPOUND_TAG,
            SyncSkillPayload::skillBox,
            SyncSkillPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
