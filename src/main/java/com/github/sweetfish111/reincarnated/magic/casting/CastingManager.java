package com.github.sweetfish111.reincarnated.magic.casting;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.event.CalculationCapacityOverException;
import com.github.sweetfish111.reincarnated.event.MasoShortageException;
import com.github.sweetfish111.reincarnated.magic.compiler.MagicCompiler;
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

    public static void startCasting(ServerPlayer player, MagiculeCircuit circuit, String triggerType) {

        //くみ上げられたノードから静的に計算コストを算出。
        int totalCalcCost = 0;
        for (MagiculeCircuit.NodeData node : circuit.getNodes()) {
            totalCalcCost += node.type.getCastCost();
        }

        boolean hasChantCancellation = checkChantCancellationSkill(player);


        if (hasChantCancellation || totalCalcCost <= 0) {
            try {
                MagicCompiler.compileAndExecute(circuit, player, triggerType);
            } catch (CalculationCapacityOverException c) {
                player.sendSystemMessage(Component.literal("《告》個体名" + player.getName() + "の演算容量が限界を超過。術式暴走が発生"));
                triggerThermalRunawayPenalty(player.level(), player);
            } catch (MasoShortageException m) {
                player.sendSystemMessage(Component.literal("《告》個体名" + player.getName().getString() + "の魔素残量が低下。術式を維持できません"));

            }
            return;
        }

        // 3. 詠唱タスクをキューに登録
        CastingTask task = new CastingTask(player, circuit, triggerType, hasChantCancellation ? 0 : totalCalcCost);
        activeCasts.put(player.getUUID(), task);
    }

    public static void releaseCasting(ServerPlayer player, String triggerType){
        CastingTask task = activeCasts.get(player.getUUID());
        if(task == null)return;

        if(task.isReady()){
            try{
                MagicCompiler.compileAndExecute(task.getCircuit(), player, task.getTriggerType());
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
        if (activeCasts.isEmpty()) return;

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

            if(!isReady){
                level.sendParticles(
                        ParticleTypes.ENCHANT,
                        player.getX(), player.getY() + 1.5, player.getZ(),
                        3, 0.5, 0.1, 0.5, 0.05
                );
            }else{
                level.sendParticles(
                        ParticleTypes.CRIT,
                        player.getX(), player.getY() + 1.5, player.getZ(),
                        2, 0.5, 0.3, 0.5, 0.1
                );
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
        level.explode(caster, caster.getX(), caster.getY(), caster.getZ(), 10.0f, Level.ExplosionInteraction.MOB);
        caster.hurt(level.damageSources().magic(), 10.0f);
    }
}
