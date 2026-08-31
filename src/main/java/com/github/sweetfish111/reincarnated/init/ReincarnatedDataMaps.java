package com.github.sweetfish111.reincarnated.init;

import com.github.sweetfish111.reincarnated.datamap.InnateSkills;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

public class ReincarnatedDataMaps {
    public static final DataMapType<EntityType<?>, InnateSkills> INNATE_SKILLS =
            DataMapType.builder(
                    Identifier.fromNamespaceAndPath("reincarnated", "innate_skills"),
                    Registries.ENTITY_TYPE,
                    InnateSkills.CODEC
            ).build(); // .synced() は付けない(サーバー専用ロジックのため)

    @SubscribeEvent // モッドイベントバス
    public static void register(RegisterDataMapTypesEvent event) {
        event.register(INNATE_SKILLS);
    }
}