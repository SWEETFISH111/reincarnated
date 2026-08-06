/*
package com.github.sweetfish111.reincarnated.event.handler;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.circuit.RuntimeMagicCircuit;
import com.github.sweetfish111.reincarnated.magic.caster.IMagicCaster;
import com.github.sweetfish111.reincarnated.magic.caster.PlayerCasterAdapter;
import com.github.sweetfish111.reincarnated.magic.casting.CastingManager;
import com.github.sweetfish111.reincarnated.magic.compiler.MagicCompiler;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = "reincarnated")
public class SpellBookUseHandler {

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        // サーバー側でのみ処理を実行
        if (event.getLevel().isClientSide()) return;

        // メインハンドに持っているアイテムが「白紙の本」かチェック
        ItemStack stack = event.getItemStack();
        if (stack.is(Items.BOOK)) {
            // NBT（CustomData）から回路データを取り出す
            CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
            if (customData != null) {
                CompoundTag tag = customData.copyTag();

                // 回路データが存在するか確認してデシリアライズ
                if (tag.contains("Nodes")) {
                    MagiculeCircuit circuit = new MagiculeCircuit();
                    circuit.loadFromNBT(tag);

                    if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                        // 1. 私たちが作った IMagicCaster アダプターで包む！
                        IMagicCaster caster = new PlayerCasterAdapter(serverPlayer);
                        ServerLevel level = (ServerLevel) serverPlayer.level();

                        CastingManager.startCasting(new MagicContext(circuit, MagicCompiler.compileCircuit(caster,circuit)));
                        // 2. 魔法コンパイラを回して発動！
                        // MagicCompiler.compileAndExecute(circuit, caster, level); // ← 実際のコンパイラ呼び出しメソッドに合わせて調整

                        // 3. アクションの成功を返してバニラの挙動を抑止
                        event.setCancellationResult(InteractionResult.SUCCESS);
                        event.setCanceled(true);
                    }
                }
            }
        }
    }
}

 */