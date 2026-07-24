package com.github.sweetfish111.reincarnated.entity;

import com.github.sweetfish111.reincarnated.magic.CustomMagicProjectileEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class reincarnatedEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(Registries.ENTITY_TYPE, "reincarnated");
    public static final ResourceKey<EntityType<?>> PROJECTILE_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("reincarnated", "custom_magic_projectile"));

    public static final Supplier<EntityType<CustomMagicProjectileEntity>> CUSTOM_MAGIC_PROJECTILE =

            ENTITY_TYPES.register("custom_magic_projectile", () ->
                    EntityType.Builder.<CustomMagicProjectileEntity>of(CustomMagicProjectileEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F) // 当たり判定のサイズ（幅・高さ）
                            .clientTrackingRange(4) // クライアントへの同期範囲
                            .updateInterval(20)     // 同期頻度
                            .build(PROJECTILE_KEY)
            );
}
/*
    public static <T> ResourceKey<T> create(ResourceKey<? extends Registry<T>> registryName, Identifier location) {
        return create(registryName.identifier, location);
    }

        public static <T> ResourceKey<Registry<T>> createRegistryKey(Identifier identifier) {
        return create(Registries.ROOT_REGISTRY_NAME, identifier);
    }

      public static Identifier fromNamespaceAndPath(String namespace, String path) {
        return createUntrusted(namespace, path);
 */