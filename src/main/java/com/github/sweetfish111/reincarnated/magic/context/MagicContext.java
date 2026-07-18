package com.github.sweetfish111.reincarnated.magic.context;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;

public class MagicContext {
    final ServerPlayer caster;
    private final MagiculeCircuit circuit;
    int currentCount = 0;
    static final int MAX_LIMIT = 1000;


    public MagicContext(ServerPlayer caster,MagiculeCircuit circuit){
        this.circuit = circuit;
        this.caster = caster;
    }

    public MagiculeCircuit getCircuit(){
        return this.circuit;
    }
    public ServerPlayer getCaster(){
        return this.caster;
    }
}
