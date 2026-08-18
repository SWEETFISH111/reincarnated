package com.github.sweetfish111.reincarnated.registry;

import com.github.sweetfish111.reincarnated.reincarnated;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public class ReincarnatedDamageTypes {
    public static final ResourceKey<DamageType> MAGIC_DAMAGE = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath(reincarnated.MODID, "magic_damage")
    );
}
