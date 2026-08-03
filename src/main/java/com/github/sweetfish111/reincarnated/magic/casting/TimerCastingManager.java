package com.github.sweetfish111.reincarnated.magic.casting;

import com.github.sweetfish111.reincarnated.circuit.RuntimeMagicCircuit;
import com.github.sweetfish111.reincarnated.event.CalculationCapacityOverException;
import com.github.sweetfish111.reincarnated.event.MasoShortageException;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.magic.nodes.MagicNode;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class TimerCastingManager {
    private static final List<TimerMagicTask> timerTasks = new ArrayList<>();
    private static final List<TimerMagicTask> pendingTasks = new ArrayList<>();

    public static void registerTimer(UUID nextNodeId, UUID repeatNodeId, MagicContext context, int delayTicks, int intervalTicks, int count) {
        pendingTasks.add(new TimerMagicTask(nextNodeId, repeatNodeId, context, delayTicks, intervalTicks, count));
    }

    public static void executeNextNode(ServerPlayer player, UUID nextNodeId, MagicContext context){
        try {
            AbstractMagicNode nextNode = context.getRuntimeNode(nextNodeId);
            nextNode.execute(context);
        } catch (CalculationCapacityOverException c) {
            context.getCaster().sendSystemMessage(Component.literal("《告》個体名" + context.getCaster().getName() + "の演算容量が限界を超過。術式暴走が発生"));
        } catch (MasoShortageException m) {
            context.getCaster().sendSystemMessage(Component.literal("《告》個体名" + context.getCaster().getName().getString() + "の魔素残量が低下。術式を維持できません"));
        }
    }

    public static void onServerTick() {
        if(!pendingTasks.isEmpty()){
            timerTasks.addAll(pendingTasks);
            pendingTasks.clear();
        }

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
                    Map<UUID, AbstractMagicNode> instancedNode = task.getContext().getRuntimeCircuit();
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
}
