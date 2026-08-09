package com.github.sweetfish111.reincarnated.effect;

import com.github.sweetfish111.reincarnated.reincarnated;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ReincarnatedEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, reincarnated.MODID);

    // オーバーチャージ（魔素過剰状態）の登録
    public static final DeferredHolder<MobEffect, MobEffect> OVERCHARGE =
            MOB_EFFECTS.register("overcharge",
                    () -> new OverchargeEffect(MobEffectCategory.BENEFICIAL, 0x8A2BE2)); // 発光色: バイオレット

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}