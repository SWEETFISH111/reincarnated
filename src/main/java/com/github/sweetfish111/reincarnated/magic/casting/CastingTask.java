package com.github.sweetfish111.reincarnated.magic.casting;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.magic.caster.IMagicCaster;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
//魔法の情報を記録して発動要求から実際の発動迄保持するクラス
public class CastingTask {
    private final UUID playerId;
    private final IMagicCaster caster;
    private final MagicContext context;
    private int remainingTicks;
    private boolean ready = false;

    public CastingTask(MagicContext context, int remainingTicks) {
        this.playerId = context.getCaster().getCasterId();
        this.caster = context.getCaster();
        this.remainingTicks = remainingTicks;
        this.context = context;
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
    public IMagicCaster getPlayer() { return caster; }
    public MagicContext getContext(){return this.context;}
    public boolean isReady(){return this.ready;}

}
