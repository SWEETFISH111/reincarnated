package com.github.sweetfish111.reincarnated.system;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@EventBusSubscriber(modid = "reincarnated")
public class MessageScheduler {

    // スレッドセーフなタスクリスト
    private static final List<MessageTask> tasks = new CopyOnWriteArrayList<>();

    /**
     * 複数メッセージを指定した間隔（Tick）で順次送信予約する
     * @param player 送信先プレイヤー
     * @param messages メッセージのリスト
     * @param intervalTicks メッセージ間の遅延（20Ticks = 1秒）
     */
    public static void scheduleMessages(ServerPlayer player, List<Component> messages, int intervalTicks) {
        int currentDelay = 0;
        for (Component msg : messages) {
            tasks.add(new MessageTask(player, msg, currentDelay));
            currentDelay += intervalTicks; // 次のメッセージの待機時間を加算
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (tasks.isEmpty()) return;

        // 毎Tickカウントを進め、完了したタスクを除去
        tasks.removeIf(MessageTask::tick);
    }
}
