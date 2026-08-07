package com.github.sweetfish111.reincarnated.magic.casting;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.circuit.RuntimeMagicCircuit;
import com.github.sweetfish111.reincarnated.event.CalculationCapacityOverException;
import com.github.sweetfish111.reincarnated.event.MasoShortageException;
import com.github.sweetfish111.reincarnated.magic.caster.IMagicCaster;
import com.github.sweetfish111.reincarnated.magic.caster.PlayerCasterAdapter;
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
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//魔法の計算コストから詠唱時間を算出して実際に発動させるクラス
public class CastingManager {
    private static final Map<UUID, CastingTask>activeCasts = new ConcurrentHashMap<>();

    public static void startCasting(MagicContext context) {

        //くみ上げられたノードから静的に計算コストを算出。
        if(activeCasts.containsKey(context.getCaster().getCasterId())){
            return;
        }

        int totalCalcCost = 0;
        for (MagiculeCircuit.NodeData node : context.getCircuit().getNodes()) {
            totalCalcCost += node.type.getCastCost();
        }

        boolean hasChantCancellation = false;

        IMagicCaster iMagicCaster = context.getCaster();
        if(iMagicCaster.getCasterEntity() instanceof ServerPlayer player){
            checkChantCancellationSkill(player);
        }


        if (hasChantCancellation || totalCalcCost <= 0) {
            RuntimeMagicCircuit runtimeMagicCircuit = MagicCompiler.compileCircuit(context.getCaster(), context.getCircuit());
            Objects.requireNonNull(runtimeMagicCircuit).start(context);
            return;
        }

        // 3. 詠唱タスクをキューに登録
        CastingTask task = new CastingTask(context, totalCalcCost);
        activeCasts.put(context.getCaster().getCasterId(), task);
    }

    public static void releaseCasting(IMagicCaster caster){
        CastingTask task = activeCasts.get(caster.getCasterId());

        if(task == null){
            activeCasts.remove(caster.getCasterId());
            return;
        }

        if(task.isReady()){
            RuntimeMagicCircuit runtimeMagicCircuit = MagicCompiler.compileCircuit(task.getContext().getCaster(), task.getContext().getCircuit());
            Objects.requireNonNull(runtimeMagicCircuit).start(task.getContext());
        }

        activeCasts.remove(caster.getCasterId());
    }

    public static void onServerTick() {
        if (!activeCasts.isEmpty()) {

            Iterator<Map.Entry<UUID, CastingTask>> iterator = activeCasts.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<UUID, CastingTask> entry = iterator.next();
                CastingTask task = entry.getValue();
                IMagicCaster caster = task.getPlayer();

                if (caster == null) {
                    iterator.remove();
                    continue;
                }

                // 詠唱中の演出（足元に魔素のパーティクルを出す）
                ServerLevel level = caster.getCasterLevel();
                boolean isReady = task.tick();
                Vec3 position = caster.getCasterPosition();

                if (!isReady) {
                    level.sendParticles(
                            ParticleTypes.ENCHANT,
                            position.x, position.y + 1.5, position.z,
                            3, 0.5, 0.1, 0.5, 0.05
                    );
                } else {
                    level.sendParticles(
                            ParticleTypes.CRIT,
                            position.x, position.y + 1.5, position.z,
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

    public static void cancelCasting(UUID casterId) {
        activeCasts.remove(casterId);
    }

}
