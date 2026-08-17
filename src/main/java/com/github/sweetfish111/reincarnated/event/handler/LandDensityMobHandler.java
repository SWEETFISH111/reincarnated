package com.github.sweetfish111.reincarnated.event.handler;

import com.github.sweetfish111.reincarnated.config.BalanceConfig;
import com.github.sweetfish111.reincarnated.item.ReincarnatedItems;
import com.github.sweetfish111.reincarnated.item.ReincarnatedItems;
import com.github.sweetfish111.reincarnated.world.LandMasoDensityData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

@EventBusSubscriber(modid = "reincarnated")
public class LandDensityMobHandler {

    private static final Identifier POWER_MODIFIER_ID =
            Identifier.fromNamespaceAndPath("reincarnated", "land_density_power");
    private static final String NBT_POWER_KEY = "reincarnated_power_multiplier";

    @SubscribeEvent
    public static void onMobSpawn(EntityJoinLevelEvent event) {
        if (event.loadedFromDisk()) return; // チャンクロード時の再適用を避ける（新規スポーンのみ対象）
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        float density = LandMasoDensityData.get(level).getDensity(level, mob.blockPosition());
        double multiplier = LandMasoDensityData.computePowerMultiplier(density);

        applyAttributeBonus(mob, Attributes.MAX_HEALTH, multiplier);
        applyAttributeBonus(mob, Attributes.ATTACK_DAMAGE, multiplier);
        mob.setHealth(mob.getMaxHealth());

        mob.getPersistentData().putFloat(NBT_POWER_KEY, (float) multiplier);
    }

    private static void applyAttributeBonus(Mob mob, Holder<Attribute> attribute, double multiplier) {
        AttributeInstance instance = mob.getAttribute(attribute);
        if (instance == null) return;

        instance.removeModifier(POWER_MODIFIER_ID);
        if (Math.abs(multiplier - 1.0) > 0.001) {
            instance.addPermanentModifier(new AttributeModifier(
                    POWER_MODIFIER_ID, multiplier - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }

    @SubscribeEvent
    public static void onMobDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;

        float multiplier = mob.getPersistentData().getFloatOr(NBT_POWER_KEY, 1.0f);
        if (multiplier <= 1.0f) return; // 基準濃度以下の地帯では追加ドロップなし

        double dropChance = computeMasoStoneDropChance(multiplier);
        if (mob.getRandom().nextDouble() < dropChance) {
            event.getDrops().add(new ItemEntity(
                    mob.level(), mob.getX(), mob.getY(), mob.getZ(),
                    new ItemStack(ReincarnatedItems.MASO_STONE.get())
            ));
        }
    }

    private static double computeMasoStoneDropChance(float multiplier) {
        double base = BalanceConfig.MASO_STONE_BASE_DROP_CHANCE.get();
        double scale = BalanceConfig.MASO_STONE_DROP_SCALE.get();
        double raw = base + (multiplier - 1.0) * scale;
        return Math.min(BalanceConfig.MASO_STONE_MAX_DROP_CHANCE.get(), Math.max(0, raw));
    }
}