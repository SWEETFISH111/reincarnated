package com.github.sweetfish111.reincarnated.system;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class MessageTask {
    private final ServerPlayer player;
    private final Component message;
    private int delayTicks; // 残り待機Tick数

    public MessageTask(ServerPlayer player, Component message, int delayTicks) {
        this.player = player;
        this.message = message;
        this.delayTicks = delayTicks;
    }

    public boolean tick() {
        this.delayTicks--;
        if (this.delayTicks <= 0) {
            if (player != null && player.connection != null) {
                player.sendSystemMessage(message);
            }
            return true; // 処理完了
        }
        return false; // 継続中
    }
}