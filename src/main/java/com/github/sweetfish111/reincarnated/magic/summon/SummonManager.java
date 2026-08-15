package com.github.sweetfish111.reincarnated.magic.summon;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class SummonManager {
    private static final Map<UUID, SummonedInstance> summonRegistry = new ConcurrentHashMap<>();
    private static final Map<UUID, List<UUID>> ownerIndex = new ConcurrentHashMap<>();
    private static final int MAX_SUMMONS_PER_OWNER = 3;

    /**
     * 召喚物を新規作成する。同一オーナーの召喚数が上限を超える場合、一番古いものを解除する。
     */
    public static UUID createSummon(UUID ownerId, Vec3 position, int livingTicks, SummonBehavior behavior) {
        List<UUID> owned = ownerIndex.computeIfAbsent(ownerId, k -> new CopyOnWriteArrayList<>());
        if (owned.size() >= MAX_SUMMONS_PER_OWNER) {
            UUID oldest = owned.get(0);
            unregisterSummon(oldest);
        }

        UUID summonId = UUID.randomUUID();
        SummonedInstance instance = new SummonedInstance(summonId, ownerId, position, livingTicks, behavior);
        summonRegistry.put(summonId, instance);
        ownerIndex.computeIfAbsent(ownerId, k -> new CopyOnWriteArrayList<>()).add(summonId);
        return summonId;
    }

    public static void unregisterSummon(UUID summonId) {
        SummonedInstance removed = summonRegistry.remove(summonId);
        if (removed != null) {
            List<UUID> owned = ownerIndex.get(removed.getOwnerId());
            if (owned != null) owned.remove(summonId);
        }
    }

    public static void unregisterAllForOwner(UUID ownerId) {
        List<UUID> owned = ownerIndex.remove(ownerId);
        if (owned != null) {
            for (UUID summonId : owned) {
                summonRegistry.remove(summonId);
            }
        }
    }

    public static SummonedInstance getSummon(UUID summonId) {
        return summonRegistry.get(summonId);
    }

    public static void onServerTick(ServerLevel level) {
        if (summonRegistry.isEmpty()) return;

        List<SummonedInstance> snapshot = new ArrayList<>(summonRegistry.values());
        for (SummonedInstance instance : snapshot) {
            if (instance.tickLifespan()) {
                unregisterSummon(instance.getSummonId());
                continue;
            }

            applyBehavior(level, instance);
            renderPresence(level, instance);
        }
    }

    private static void applyBehavior(ServerLevel level, SummonedInstance instance) {
        if (instance.getBehavior() == SummonBehavior.DECOY) {
            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(instance.getOwnerId());
            if (owner != null) {
                // 視線方向を模倣する（移動の模倣は将来の拡張課題として据え置き）
                instance.setYRot(owner.getYRot());
                instance.setXRot(owner.getXRot());
            }
        }
    }

    private static void renderPresence(ServerLevel level, SummonedInstance instance) {
        Vec3 pos = instance.getPosition();
        level.sendParticles(
                ParticleTypes.ENCHANT,
                pos.x, pos.y + 1.0, pos.z,
                2, 0.2, 0.4, 0.2, 0.02
        );
    }

    /** 召喚した瞬間の演出（呼び出し側から任意で使う） */
    public static void playSummonEffects(ServerLevel level, Vec3 pos) {
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.7f, 1.4f);
        level.sendParticles(ParticleTypes.WITCH, pos.x, pos.y + 0.5, pos.z, 20, 0.3, 0.5, 0.3, 0.05);
    }
}