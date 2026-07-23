package com.github.sweetfish111.reincarnated.magic.casting;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class CastingTask {
    private final UUID playerId;
    private final ServerPlayer player;
    private final MagiculeCircuit circuit;
    private final String triggerType;
    private int remainingTicks;
    private final int totalTicks;
    private boolean ready = false;

    public CastingTask(ServerPlayer player, MagiculeCircuit circuit, String triggerType, int totalTicks) {
        this.playerId = player.getUUID();
        this.player = player;
        this.circuit = circuit;
        this.triggerType = triggerType;
        this.totalTicks = totalTicks;
        this.remainingTicks = totalTicks;
    }

    public boolean tick(){
        if (remainingTicks > 0) {
            remainingTicks--;
            if (remainingTicks <= 0) {
                ready = true; // 詠唱完了！発動待機状態へ
            }
        }
        return ready;
    }
    public UUID getPlayerId() { return playerId; }
    public ServerPlayer getPlayer() { return player; }
    public MagiculeCircuit getCircuit() { return circuit; }
    public String getTriggerType() { return triggerType; }
    public int getRemainingTicks() { return remainingTicks; }
    public int getTotalTicks() { return totalTicks; }
    public boolean isReady(){return ready;}
}
