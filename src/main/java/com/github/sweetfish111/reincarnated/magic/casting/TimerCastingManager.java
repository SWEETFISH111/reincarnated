package com.github.sweetfish111.reincarnated.magic.casting;

import com.github.sweetfish111.reincarnated.circuit.RuntimeMagicCircuit;
import com.github.sweetfish111.reincarnated.magic.caster.IMagicCaster;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.magic.nodes.MagicNode;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class TimerCastingManager {
    private static final List<TimerMagicTask> timerTasks = new CopyOnWriteArrayList<>();
    private static final List<TimerMagicTask> pendingTasks = new CopyOnWriteArrayList<>();
    private static final List<UUID> canselingTasks = new CopyOnWriteArrayList<>();

    public static void registerTimer(UUID nextNodeId, UUID repeatNodeId, MagicContext context, int delayTicks, int intervalTicks, int count) {
        pendingTasks.add(new TimerMagicTask(nextNodeId, repeatNodeId, context, delayTicks, intervalTicks, count));
    }

    public static void cancelTasksByRepeatNode(List<UUID> repeatNodeId) {
        if (repeatNodeId != null && !repeatNodeId.isEmpty()) {
            canselingTasks.addAll(repeatNodeId);
        }
    }

    public static void executeNextNode(IMagicCaster caster, UUID nextNodeId, MagicContext context){
        RuntimeMagicCircuit.executeNode(caster, nextNodeId, context);
    }

    public static void onServerTick() {
        // 1. 新規タスクの合流
        if(!pendingTasks.isEmpty()){
            timerTasks.addAll(pendingTasks);
            pendingTasks.clear();
        }

        // 2. キャンセル処理の安全な適用
        if(!canselingTasks.isEmpty()){
            List<UUID> targets = new ArrayList<>(canselingTasks);
            canselingTasks.clear();
            for(UUID cancelNodeId : targets){
                pendingTasks.removeIf(timerMagicTask -> timerMagicTask.getRepeatNodeId().equals(cancelNodeId));
                timerTasks.removeIf(timerMagicTask -> timerMagicTask.getRepeatNodeId().equals(cancelNodeId));
            }
        }

        // 3. タイマーの進行と実行（軽量なイテレート）
        Iterator<TimerMagicTask> iterator = timerTasks.iterator();
        while (iterator.hasNext()) {
            TimerMagicTask task = iterator.next();
            if (task.tick()) {
                MagicContext ctx = task.getContext();
                UUID targetId = task.getNextNodeId();

                ctx.setNodeLocalVariable(task.getRepeatNodeId(), 0, task.getCurrentLoopIndex());
                if(targetId != null) {
                    executeNextNode(ctx.getCaster(), targetId, ctx);
                }

                if (task.hasMore()) {
                    task.resetTimer();
                } else {
                    iterator.remove();
                    Map<UUID, AbstractMagicNode> instancedNode = task.getContext().getRuntimeCircuit().getInstancedNodes();
                    AbstractMagicNode repeatNode = instancedNode.get(task.getRepeatNodeId());
                    if(repeatNode != null){
                        MagicNode nextNode = repeatNode.getNextNode(2);
                        if(nextNode != null){
                            executeNextNode(task.getContext().getCaster(), ((AbstractMagicNode)nextNode).getId(), task.getContext());
                        }
                    }
                }
            }
        }
    }

    public static void cancelTasksForCaster(UUID casterUuid) {
        if (casterUuid == null) return;

        timerTasks.removeIf(task -> {
            IMagicCaster caster = task.getContext().getCaster();
            return caster != null && caster.getCasterId().equals(casterUuid);
        });
        pendingTasks.removeIf(task -> {
            IMagicCaster caster = task.getContext().getCaster();
            return caster != null && caster.getCasterId().equals(casterUuid);
        });
    }
}