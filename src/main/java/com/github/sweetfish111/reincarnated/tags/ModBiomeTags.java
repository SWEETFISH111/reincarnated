package com.github.sweetfish111.reincarnated.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class ModBiomeTags {
    public static final TagKey<Biome> MASO_DENSITY_VERY_HIGH = create("maso_density_very_high");
    public static final TagKey<Biome> MASO_DENSITY_HIGH = create("maso_density_high");
    public static final TagKey<Biome> MASO_DENSITY_LOW = create("maso_density_low");
    public static final TagKey<Biome> MASO_DENSITY_VERY_LOW = create("maso_density_very_low");

    private static TagKey<Biome> create(String name) {
        return TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("reincarnated", name));
    }
}