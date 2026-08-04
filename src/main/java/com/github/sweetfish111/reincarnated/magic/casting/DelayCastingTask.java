package com.github.sweetfish111.reincarnated.magic.casting;

import com.github.sweetfish111.reincarnated.circuit.RuntimeMagicCircuit;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;

public class DelayCastingTask {
    private final UUID playerId;
    private final ServerPlayer player;
    private final MagicContext context;
    private final UUID nextNodeId;
    private int delayTicks;
    private RuntimeMagicCircuit runtimeCircuit;
    private boolean ready = false;

    public DelayCastingTask(UUID playerId, ServerPlayer player, MagicContext context, UUID nextNodeId, int delayTicks){
        this. playerId = playerId;
        this.player = player;
        this.context = context;
        this.nextNodeId = nextNodeId;
        this.delayTicks = delayTicks;
        this.runtimeCircuit = context.getRuntimeCircuit();
    }

    public boolean tick(){
        if (delayTicks > 0) {
            delayTicks--;
            if (delayTicks <= 0) {
                ready = true; // 詠唱完了！発動待機状態へ
            }
        }
        return ready;
    }

    public ServerPlayer getPlayer() { return player; }
    public UUID getNextNodeId(){return this.nextNodeId;}
    public MagicContext getContext(){return this.context;}
}
