package com.github.sweetfish111.reincarnated.magic.context;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.circuit.RuntimeMagicCircuit;
import com.github.sweetfish111.reincarnated.event.CalculationCapacityOverException;
import com.github.sweetfish111.reincarnated.magic.caster.CasterSnapshot;
import com.github.sweetfish111.reincarnated.magic.caster.IMagicCaster;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MagicContext {
    final IMagicCaster caster;
    private final MagiculeCircuit circuit;
    private final RuntimeMagicCircuit runtimeCircuit;
    private final CasterSnapshot snapshot;
    private final Map<UUID, Map<Integer, Double>> LocalVariable = new HashMap<>();
    private final Map<String, Object> magicValue = new HashMap<>();
    private final ExecutionTrace trace = new ExecutionTrace();
    private final ServerLevel level;
    private int currentCount = 0;
    private static final int MAX_LIMIT = 1000;

    public MagicContext(MagiculeCircuit circuit, RuntimeMagicCircuit runtimeCircuit) {
        this.circuit = circuit;
        this.runtimeCircuit = runtimeCircuit;
        this.caster = runtimeCircuit.getCaster();
        this.level = caster.getCasterLevel();
        this.snapshot = CasterSnapshot.capture(caster);
    }

    public ServerLevel getLevel() {
        return this.level;
    }

    public MagiculeCircuit getCircuit() {
        return this.circuit;
    }

    public IMagicCaster getCaster() {
        return this.caster;
    }
    public double getNodeLocalVariable(UUID nodeId, int portIndex){
        Map<Integer, Double> vars = this.LocalVariable.get(nodeId);
        if(vars != null && vars.containsKey(portIndex)){
            double val = vars.get(portIndex);
            return val;
        }
        return 0;
    }
    public Object getMagicValue(String key){
        return this.magicValue.get(key);
    }
    public CasterSnapshot getSnapshot(){return this.snapshot;}
    public RuntimeMagicCircuit getRuntimeCircuit(){return this.runtimeCircuit;}
    public AbstractMagicNode getRuntimeNode(UUID id){return this.runtimeCircuit.getInstancedNode(id);}

    public void setNodeLocalVariable(UUID nodeId, int portIndex, double value) {
        this.LocalVariable
                .computeIfAbsent(nodeId, k -> new HashMap<>())
                .put(portIndex, value);
    }

    public void setMagicValue(String key, Object value) {
        magicValue.put(key, value);
    }

    public void incrementAndCheck() throws CalculationCapacityOverException {
        currentCount ++;
        if(currentCount > MAX_LIMIT){
            throw new RuntimeException("告：魔法の演算容量（ループ上限）を超過しました。熱暴走によりキャストを強制中断します。");
        }
    }
}
