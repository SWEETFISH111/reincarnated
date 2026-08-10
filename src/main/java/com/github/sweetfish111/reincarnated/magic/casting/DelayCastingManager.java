package com.github.sweetfish111.reincarnated.magic.casting;

import com.github.sweetfish111.reincarnated.circuit.RuntimeMagicCircuit;
import com.github.sweetfish111.reincarnated.event.CalculationCapacityOverException;
import com.github.sweetfish111.reincarnated.event.MasoShortageException;
import com.github.sweetfish111.reincarnated.magic.caster.IMagicCaster;
import com.github.sweetfish111.reincarnated.magic.caster.PlayerCasterAdapter;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class DelayCastingManager {
    private static final java.util.List<DelayCastingTask> delayedTasks = new CopyOnWriteArrayList<>();


    public static void scheduleDelay(IMagicCaster caster, UUID nextNodeId, MagicContext context, int delayTickes){
        if(delayTickes <= 0){
            executeNextNode(caster, nextNodeId, context);
            return;
        }
        delayedTasks.add(new DelayCastingTask(caster, context, nextNodeId, delayTickes));
    }

    public static void executeNextNode(IMagicCaster caster, UUID nextNodeId, MagicContext context){
        RuntimeMagicCircuit.executeNode(caster, nextNodeId, context);
    }

    public static void cancelTasksForCaster(UUID casterUuid) {
        if (casterUuid == null) return;

        delayedTasks.removeIf(task -> {
            IMagicCaster caster = task.getCaster();
            return caster != null && caster.getCasterId().equals(casterUuid);
        });
    }

    public static void onServerTick(){
        if(!delayedTasks.isEmpty()){
            Iterator<DelayCastingTask> delayIterator = delayedTasks.iterator();
            while (delayIterator.hasNext()) {
                DelayCastingTask task = delayIterator.next();
                IMagicCaster caster = task.getCaster();

                if (caster == null) {
                    delayIterator.remove();
                    continue;
                }

                // タイマーを進め、時間が来たら発動
                if (task.tick()) {
                    if (task.getContext().isStale()) {
                        delayIterator.remove();
                        continue; // 着弾コールバック等が「もう存在しない古い回路」に対して発火するのを防ぐ
                    }
                    executeNextNode(caster, task.getNextNodeId(), task.getContext());
                    delayIterator.remove();
                }
            }
        }
    }


}
