package com.github.sweetfish111.reincarnated.magic.context;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MagicContext {
    final ServerPlayer caster;
    private final MagiculeCircuit circuit;
    private final Map<UUID, Map<Integer, Double>> LocalVariable = new HashMap<>();
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
    public void setNodeLocalVariable(UUID nodeId, int portIndex, double value){
        this.LocalVariable
                .computeIfAbsent(nodeId, k -> new HashMap<>())
                .put(portIndex, value);
    }

    public double getNodeLocalVariable(UUID nodeId, int portIndex){
        Map<Integer, Double> vars = this.LocalVariable.get(nodeId);
        if(vars != null && vars.containsKey(portIndex)){
            double val = vars.get(portIndex);
            return val;
        }
        return 0;
    }
}
