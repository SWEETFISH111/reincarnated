package com.github.sweetfish111.reincarnated.magic.casting;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;
//魔法の情報を記録して発動要求から実際の発動迄保持するクラス
public class CastingTask {
    private final UUID playerId;
    private final ServerPlayer player;
    private final MagiculeCircuit circuit;
    private final MagicContext context;
    private int remainingTicks;
    private final int totalTicks;
    private boolean ready = false;

    public CastingTask(MagicContext context, int totalTicks) {
        this.playerId = context.getCaster().getUUID();
        this.player = context.getCaster();
        this.circuit = context.getCircuit();
        this.context = context;
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
    public int getRemainingTicks() { return remainingTicks; }
    public int getTotalTicks() { return totalTicks; }
    public boolean isReady(){return ready;}
}
