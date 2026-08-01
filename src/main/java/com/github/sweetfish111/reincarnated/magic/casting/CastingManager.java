package com.github.sweetfish111.reincarnated.magic.casting;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

//魔法の計算コストから詠唱時間を算出して実際に発動させるクラス
@EventBusSubscriber(modid = "reincarnated")
public class CastingManager {
    private static final Map<UUID, CastingTask>activeCasts = new HashMap<>();
    private static final java.util.List<DelayedMagicTask> delayedTasks = new java.util.ArrayList<>();

    public static void scheduleDelay(ServerPlayer player, AbstractMagicNode currentNode, MagicContext context, int delayTickes){
        if(delayTickes <= 0){
            executeNextNode(player, currentNode, context);
            return;
        }
        delayedTasks.add(new DelayedMagicTask(player, currentNode, context, delayTickes));
    }
    public static void executeNextNode(ServerPlayer player, AbstractMagicNode currentNode, MagicContext context){
        if (currentNode == null) return;
        currentNode.pushExecute(0, context);
    }
    public static void startCasting(MagicContext context) {

        //くみ上げられたノードから静的に計算コストを算出。
        int totalCalcCost = 0;
        for (MagiculeCircuit.NodeData node : context.getCircuit().getNodes()) {
            totalCalcCost += node.type.getCastCost();
        }

        boolean hasChantCancellation = checkChantCancellationSkill(context.getCaster());


        if (hasChantCancellation || totalCalcCost <= 0) {
            try {
                MagicCompiler.compileAndExecute(context);
            } catch (CalculationCapacityOverException c) {
                context.getCaster().sendSystemMessage(Component.literal("《告》個体名" + context.getCaster().getName() + "の演算容量が限界を超過。術式暴走が発生"));
                triggerThermalRunawayPenalty(context.getCaster().level(), context.getCaster());
            } catch (MasoShortageException m) {
                context.getCaster().sendSystemMessage(Component.literal("《告》個体名" + context.getCaster().getName().getString() + "の魔素残量が低下。術式を維持できません"));

            }
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
            try{
                MagicCompiler.compileAndExecute(new MagicContext(player, task.getCircuit()));
            }catch(CalculationCapacityOverException c){
                player.sendSystemMessage(Component.literal("《告》個体名" + player.getName().getString() + "の演算容量が限界を超過。術式暴走が発生"));
                triggerThermalRunawayPenalty(player.level(), player);
            }catch (MasoShortageException m){
                player.sendSystemMessage(Component.literal("《告》個体名" + player.getName().getString() + "の魔素残量が低下。術式を維持できません"));

            }finally {
                activeCasts.remove(player.getUUID());
            }
        }else {

        }

        activeCasts.remove(player.getUUID());
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
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

        if(!delayedTasks.isEmpty()){
            Iterator<DelayedMagicTask> delayIterator = delayedTasks.iterator();
            while (delayIterator.hasNext()) {
                DelayedMagicTask task = delayIterator.next();
                ServerPlayer player = task.getPlayer();

                if (player == null || player.isRemoved()) {
                    delayIterator.remove();
                    continue;
                }

                // タイマーを進め、時間が来たら発動
                if (task.tick()) {
                    executeNextNode(player, task.getCurrentNode(), task.getContext());
                    delayIterator.remove();
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
