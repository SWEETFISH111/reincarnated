package com.github.sweetfish111.reincarnated.magic.casting;

import com.github.sweetfish111.reincarnated.circuit.RuntimeMagicCircuit;
import com.github.sweetfish111.reincarnated.magic.caster.IMagicCaster;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.context.PassiveExecutionContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.magic.nodes.MagicNode;
import com.github.sweetfish111.reincarnated.reincarnated;

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

        // 3. タイマーの進行と実行（拡張for文に変更し、終了したタスクは後からまとめて削除）
        List<TimerMagicTask> finishedTasks = new ArrayList<>();

        for (TimerMagicTask task : timerTasks) {
            if (task.tick()) {
                MagicContext ctx = task.getContext();
                UUID targetId = task.getNextNodeId();

                if (ctx.isStale()) {
                    finishedTasks.add(task); // 実行せず、この世代のループはここで静かに終了
                    reincarnated.LOGGER.debug("stale magic task discarded (repeatNode={})", task.getRepeatNodeId());
                    continue;
                }

                ctx.setNodeLocalVariable(task.getRepeatNodeId(), 0, task.getCurrentLoopIndex());
                ctx.resetCount();
                if(targetId != null) {
                    UUID finalTargetId = targetId; // ラムダ内で使うため
                    PassiveExecutionContext.runAsPassive(() -> // ★追加
                            executeNextNode(ctx.getCaster(), finalTargetId, ctx));
                }

                if (task.hasMore()) {
                    task.resetTimer();
                } else {
                    finishedTasks.add(task);
                    Map<UUID, AbstractMagicNode> instancedNode = task.getContext().getRuntimeCircuit().getInstancedNodes();
                    AbstractMagicNode repeatNode = instancedNode.get(task.getRepeatNodeId());
                    if(repeatNode != null){
                        MagicNode nextNode = repeatNode.getNextNode(2);
                        if(nextNode != null){
                            PassiveExecutionContext.runAsPassive(() -> // ★追加
                                    executeNextNode(task.getContext().getCaster(), ((AbstractMagicNode)nextNode).getId(), task.getContext()));
                        }
                    }
                }
            }
        }

        // 終了したタスクをまとめて削除
        if (!finishedTasks.isEmpty()) {
            timerTasks.removeAll(finishedTasks);
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