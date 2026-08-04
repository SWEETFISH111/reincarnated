package com.github.sweetfish111.reincarnated.magic.casting;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.circuit.RuntimeMagicCircuit;
import com.github.sweetfish111.reincarnated.event.CalculationCapacityOverException;
import com.github.sweetfish111.reincarnated.event.MasoShortageException;
import com.github.sweetfish111.reincarnated.magic.compiler.MagicCompiler;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.magic.nodes.MagicNode;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;

//魔法の計算コストから詠唱時間を算出して実際に発動させるクラス
public class CastingManager {
    private static final Map<UUID, CastingTask>activeCasts = new HashMap<>();

    private static void registerTimer(UUID nextNodeId, MagicContext context, int delayTicks, int intervalTicks, int count){

    }

    public static void startCasting(MagicContext context) {

        //くみ上げられたノードから静的に計算コストを算出。
        int totalCalcCost = 0;
        for (MagiculeCircuit.NodeData node : context.getCircuit().getNodes()) {
            totalCalcCost += node.type.getCastCost();
        }

        boolean hasChantCancellation = checkChantCancellationSkill(context.getCaster());


        if (hasChantCancellation || totalCalcCost <= 0) {
            RuntimeMagicCircuit runtimeMagicCircuit = MagicCompiler.compileCircuit(context.getCaster(), context.getCircuit());
            Objects.requireNonNull(runtimeMagicCircuit).execute(context);
            return;
        }

        // 3. 詠唱タスクをキューに登録
        CastingTask task = new CastingTask(context, hasChantCancellation ? 0 : totalCalcCost);
        activeCasts.put(context.getCaster().getUUID(), task);
    }

    public static void releaseCasting(ServerPlayer player){
        CastingTask task = activeCasts.get(player.getUUID());
        if(task == null)return;

        if(task.isReady()){
            RuntimeMagicCircuit runtimeMagicCircuit = MagicCompiler.compileCircuit(task.getContext().getCaster(), task.getContext().getCircuit());
            Objects.requireNonNull(runtimeMagicCircuit).execute(task.getContext());
        }

        activeCasts.remove(player.getUUID());
    }

    public static void onServerTick() {
        if (!activeCasts.isEmpty()) {

            Iterator<Map.Entry<UUID, CastingTask>> iterator = activeCasts.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<UUID, CastingTask> entry = iterator.next();
                CastingTask task = entry.getValue();
                ServerPlayer player = task.getPlayer();

                if (player == null || player.isRemoved()) {
                    iterator.remove();
                    continue;
                }

                // 詠唱中の演出（足元に魔素のパーティクルを出す）
                ServerLevel level = player.level();
                boolean isReady = task.tick();

                if (!isReady) {
                    level.sendParticles(
                            ParticleTypes.ENCHANT,
                            player.getX(), player.getY() + 1.5, player.getZ(),
                            3, 0.5, 0.1, 0.5, 0.05
                    );
                } else {
                    level.sendParticles(
                            ParticleTypes.CRIT,
                            player.getX(), player.getY() + 1.5, player.getZ(),
                            2, 0.5, 0.3, 0.5, 0.1
                    );
                }
            }
        }
    }
    private static boolean checkChantCancellationSkill(ServerPlayer player) {
        // TODO: 魂のデータ（Data Attachment等）から『詠唱破棄』スキルを持っているか判定する
        return false;
    }

    public static void cancelCasting(ServerPlayer player) {
        activeCasts.remove(player.getUUID());
    }

    private static void triggerThermalRunawayPenalty(ServerLevel level, ServerPlayer caster){
        level.explode(caster, caster.getX(), caster.getY(), caster.getZ(), 10.0f, Level.ExplosionInteraction.TNT);
        caster.hurt(level.damageSources().magic(), 10.0f);
    }
}
