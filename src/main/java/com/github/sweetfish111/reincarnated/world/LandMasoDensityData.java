package com.github.sweetfish111.reincarnated.world;

import com.github.sweetfish111.reincarnated.config.BalanceConfig;
import com.github.sweetfish111.reincarnated.tags.ModBiomeTags;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LandMasoDensityData extends SavedData {

    /** チャンク単位の濃度1件分のシリアライズ単位 */
    private record DensityEntry(long chunkKey, float density) {
        static final Codec<DensityEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.LONG.fieldOf("chunkKey").forGetter(DensityEntry::chunkKey),
                Codec.FLOAT.fieldOf("density").forGetter(DensityEntry::density)
        ).apply(instance, DensityEntry::new));
    }

    public static final Codec<LandMasoDensityData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("worldSeed").forGetter(d -> d.worldSeed),
            Codec.list(DensityEntry.CODEC).fieldOf("entries").forGetter(LandMasoDensityData::toEntryList)
    ).apply(instance, LandMasoDensityData::fromEntryList));

    public static final SavedDataType<LandMasoDensityData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("reincarnated", "land_maso_density"),
            (level) -> newForSeed(level != null ? level.getSeed() : 0L),
            (level) -> CODEC
    );

    private final Map<Long, Float> chunkDensityCache = new HashMap<>();
    private final Map<Long, Float> chunkCurrentCache = new HashMap<>();
    private long worldSeed;

    public static LandMasoDensityData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    private static LandMasoDensityData newForSeed(long seed) {
        LandMasoDensityData data = new LandMasoDensityData();
        data.worldSeed = seed;
        return data;
    }

    private static LandMasoDensityData fromEntryList(long worldSeed, List<DensityEntry> entries) {
        LandMasoDensityData data = new LandMasoDensityData();
        data.worldSeed = worldSeed;
        for (DensityEntry e : entries) {
            data.chunkDensityCache.put(e.chunkKey(), e.density());
        }
        return data;
    }

    private List<DensityEntry> toEntryList() {
        List<DensityEntry> list = new ArrayList<>();
        for (Map.Entry<Long, Float> e : chunkDensityCache.entrySet()) {
            list.add(new DensityEntry(e.getKey(), e.getValue()));
        }
        return list;
    }

    public float getDensity(ServerLevel level, BlockPos pos) {
        ChunkPos chunkPos = ChunkPos.containing(pos);
        long key = chunkPos.pack();
        Float cached = chunkDensityCache.get(key);
        if (cached != null) return cached;

        float generated = generateDensity(level, chunkPos);
        chunkDensityCache.put(key, generated);
        setDirty();
        return generated;
    }

    public float getCurrentAvailable(ServerLevel level, BlockPos pos){
        ChunkPos chunkPos = ChunkPos.containing(pos);
        long key = chunkPos.pack();
        Float cached = chunkCurrentCache.get(key);
        if (cached != null) return cached;

        float generated = getDensity(level, pos);
        chunkCurrentCache.put(key, generated);
        setDirty();
        return generated;
    }

    public double consumeCurrent(ServerLevel level, BlockPos pos, double amount){
        ChunkPos chunkPos = ChunkPos.containing(pos);
        long key = chunkPos.pack();
        float available = getCurrentAvailable(level, pos);
        if(available >= amount){
            float result = (float) (available - amount);
            chunkCurrentCache.put(key, result);
        }else {
            chunkCurrentCache.put(key, 0.0f);
        }
        setDirty();
        return Math.min(available, amount);
    }

    public void returnToLand(ServerLevel level, BlockPos pos, double amount){
        ChunkPos chunkPos = ChunkPos.containing(pos);
        long key = chunkPos.pack();
        float current = getCurrentAvailable(level, pos);
        current += amount;
        chunkCurrentCache.put(key, current);
        setDirty();
        double pollutionThreshold = getDensity(level, pos) * BalanceConfig.MASO_POLLUTION_THRESHOLD_RATIO.get();
        if(current > pollutionThreshold){
            //todo 汚染処理
        }
    }

    public void refillAllToMax(ServerLevel level){
        for (Long key : chunkCurrentCache.keySet()) {
            Float naturalMax = chunkDensityCache.get(key);
            if (naturalMax != null) {
                chunkCurrentCache.put(key, naturalMax);
            }
        }
        setDirty();
    }

    private float generateDensity(ServerLevel level, ChunkPos chunkPos) {
        float biomeBase = getBiomeBaseDensity(level, chunkPos);
        double distance = getDistanceFromSpawn(level, chunkPos);

        // ★修正：減衰の基準距離をMASO_DENSITY_DAMPING_DISTANCE(3000m)に変更。
        //   リング加算の基準距離(MASO_DENSITY_RING_DISTANCE)とは別の値として独立させた。
        double noiseDamping = Math.min(1.0, distance / BalanceConfig.MASO_DENSITY_DAMPING_DISTANCE.get());

        float normalBase = BalanceConfig.MASO_DENSITY_NORMAL_BASE.get().floatValue();
        float biomeDelta = biomeBase - normalBase;
        float dampedBiomeBase = normalBase + biomeDelta * (float) noiseDamping;

        float ringBonus = getRingBonus(distance); // こちらは従来通りMASO_DENSITY_RING_DISTANCE基準のまま
        float noise = valueNoise(chunkPos.x(), chunkPos.z(), worldSeed, BalanceConfig.MASO_DENSITY_NOISE_SCALE.get().floatValue());

        float density = dampedBiomeBase + ringBonus
                + noise * BalanceConfig.MASO_DENSITY_NOISE_AMPLITUDE.get().floatValue() * (float) noiseDamping;
        return Math.max(0f, density);
    }

    private double getDistanceFromSpawn(ServerLevel level, ChunkPos chunkPos) {
        BlockPos spawn = level.getRespawnData().pos();
        double dx = chunkPos.getMiddleBlockX() - spawn.getX();
        double dz = chunkPos.getMiddleBlockZ() - spawn.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    private float getRingBonus(double distance) {
        int ringTier = (int) (distance / BalanceConfig.MASO_DENSITY_RING_DISTANCE.get());
        return (float) (ringTier * BalanceConfig.MASO_DENSITY_RING_INCREMENT.get());
    }

    public static double computePowerMultiplier(float density) {
        double normalBase = BalanceConfig.MASO_DENSITY_NORMAL_BASE.get();
        double raw = normalBase > 0 ? density / normalBase : 1.0;
        return Math.max(BalanceConfig.MASO_MOB_POWER_MIN_MULTIPLIER.get(),
                Math.min(BalanceConfig.MASO_MOB_POWER_MAX_MULTIPLIER.get(), raw));
    }

    private float getBiomeBaseDensity(ServerLevel level, ChunkPos chunkPos) {
        BlockPos center = chunkPos.getMiddleBlockPosition(level.getSeaLevel());
        Holder<Biome> biome = level.getBiome(center);

        if (biome.is(ModBiomeTags.MASO_DENSITY_VERY_HIGH)) return BalanceConfig.MASO_DENSITY_VERY_HIGH_BASE.get().floatValue();
        if (biome.is(ModBiomeTags.MASO_DENSITY_HIGH))      return BalanceConfig.MASO_DENSITY_HIGH_BASE.get().floatValue();
        if (biome.is(ModBiomeTags.MASO_DENSITY_LOW))       return BalanceConfig.MASO_DENSITY_LOW_BASE.get().floatValue();
        if (biome.is(ModBiomeTags.MASO_DENSITY_VERY_LOW))  return BalanceConfig.MASO_DENSITY_VERY_LOW_BASE.get().floatValue();
        return BalanceConfig.MASO_DENSITY_NORMAL_BASE.get().floatValue();
    }

    private static float hash(long seed, int x, int z) {
        long h = seed ^ ((long) x * 0x9E3779B97F4A7C15L) ^ ((long) z * 0xC2B2AE3D27D4EB4FL);
        h = (h ^ (h >>> 33)) * 0xFF51AFD7ED558CCDL;
        h = (h ^ (h >>> 33)) * 0xC4CEB9FE1A85EC53L;
        h = h ^ (h >>> 33);
        return ((h & 0xFFFFFF) / (float) 0xFFFFFF) * 2f - 1f;
    }

    private static float smooth(float t) { return t * t * (3f - 2f * t); }
    private static float lerp(float a, float b, float t) { return a + (b - a) * t; }

    private static float valueNoise(int chunkX, int chunkZ, long seed, float scale) {
        float gx = chunkX / scale;
        float gz = chunkZ / scale;
        int x0 = (int) Math.floor(gx);
        int z0 = (int) Math.floor(gz);
        float tx = smooth(gx - x0);
        float tz = smooth(gz - z0);

        float v00 = hash(seed, x0, z0);
        float v10 = hash(seed, x0 + 1, z0);
        float v01 = hash(seed, x0, z0 + 1);
        float v11 = hash(seed, x0 + 1, z0 + 1);

        float ix0 = lerp(v00, v10, tx);
        float ix1 = lerp(v01, v11, tx);
        return lerp(ix0, ix1, tz);
    }
}