package com.github.sweetfish111.reincarnated.item;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.magic.caster.IMagicCaster;
import com.github.sweetfish111.reincarnated.magic.caster.PlayerCasterAdapter;
import com.github.sweetfish111.reincarnated.magic.casting.CastingManager;
import com.github.sweetfish111.reincarnated.magic.compiler.MagicCompiler;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public class GrimoireItem extends Item {
    public GrimoireItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.SPEAR; // 構えモーション
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000; // 長押し上限
    }



    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        System.out.println("GrimoireItem:use");

        // サーバー側で詠唱タスク開始
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
            if (customData != null) {
                CompoundTag tag = customData.copyTag();
                if (tag.contains("Nodes")) {
                    MagiculeCircuit circuit = new MagiculeCircuit();
                    circuit.loadFromNBT(tag);

                    IMagicCaster caster = new PlayerCasterAdapter(serverPlayer);
                    CastingManager.startCasting(new MagicContext(circuit, MagicCompiler.compileCircuit(caster, circuit)));
                }
            }
        }

        player.startUsingItem(hand);

        // プレイヤーにチャージ（使用）を開始させる
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity entity) {
        System.out.println("GrimoireItem:finishUsingItem");
        return super.finishUsingItem(itemStack, level, entity);
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        System.out.println("GrimoireItem:releaseUsing");
        if (!level.isClientSide() && entity instanceof ServerPlayer serverPlayer) {
            IMagicCaster caster = new PlayerCasterAdapter(serverPlayer);
            // 離した瞬間の解放処理（CastingManager側でタスク削除や発動判定を行う）
            CastingManager.releaseCasting(caster);
        }
        return true;
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
        System.out.println("GrimoireItem:onStopUsing");
        super.onStopUsing(stack, entity, count);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}