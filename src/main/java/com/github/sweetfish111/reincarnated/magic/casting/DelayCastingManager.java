package com.github.sweetfish111.reincarnated.magic.casting;

import com.github.sweetfish111.reincarnated.event.CalculationCapacityOverException;
import com.github.sweetfish111.reincarnated.event.MasoShortageException;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class DelayCastingManager {
    private static final java.util.List<DelayCastingTask> delayedTasks = new java.util.ArrayList<>();


    public static void scheduleDelay(ServerPlayer player, UUID nextNodeId, MagicContext context, int delayTickes){
        if(delayTickes <= 0){
            executeNextNode(player, nextNodeId, context);
            return;
        }
        delayedTasks.add(new DelayCastingTask(player.getUUID(), player, context, nextNodeId, delayTickes));
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

    public static void onServerTick(){
        if(!delayedTasks.isEmpty()){
            Iterator<DelayCastingTask> delayIterator = delayedTasks.iterator();
            while (delayIterator.hasNext()) {
                DelayCastingTask task = delayIterator.next();
                ServerPlayer player = task.getPlayer();

                if (player == null || player.isRemoved()) {
                    delayIterator.remove();
                    continue;
                }

                // タイマーを進め、時間が来たら発動
                if (task.tick()) {
                    executeNextNode(player, task.getNextNodeId(), task.getContext());
                    delayIterator.remove();
                }
            }
        }
    }


}
