package com.github.sweetfish111.reincarnated.magic.casting;

import com.github.sweetfish111.reincarnated.circuit.RuntimeMagicCircuit;
import com.github.sweetfish111.reincarnated.magic.caster.IMagicCaster;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;

public class DelayCastingTask {
    private final IMagicCaster caster;
    private final MagicContext context;
    private final UUID nextNodeId;
    private int delayTicks;
    private boolean ready = false;

    public DelayCastingTask(IMagicCaster caster, MagicContext context, UUID nextNodeId, int delayTicks){
        this.caster = caster;
        this.context = context;
        this.nextNodeId = nextNodeId;
        this.delayTicks = delayTicks;
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

    public IMagicCaster getCaster() { return caster; }
    public UUID getNextNodeId(){return this.nextNodeId;}
    public MagicContext getContext(){return this.context;}
}
