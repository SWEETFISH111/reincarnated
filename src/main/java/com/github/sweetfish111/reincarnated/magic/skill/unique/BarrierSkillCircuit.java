package com.github.sweetfish111.reincarnated.magic.skill.unique;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.circuit.MagiculeNodeType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BarrierSkillCircuit {
    public static List<UUID> getBarrierSkillCircuit(MagiculeCircuit circuit){
        MagiculeCircuit.NodeData triggerNode = new MagiculeCircuit.NodeData(UUID.randomUUID(), MagiculeNodeType.ON_TICK, 50, 10);
        MagiculeCircuit.NodeData rateNode = new MagiculeCircuit.NodeData(UUID.randomUUID(), MagiculeNodeType.NUMBER, 150, 20);
        MagiculeCircuit.NodeData addBarrier = new MagiculeCircuit.NodeData(UUID.randomUUID(), MagiculeNodeType.BARRIER, 250, 10);

        circuit.addNode(triggerNode);
        circuit.addNode(rateNode);
        circuit.addNode(addBarrier);

        MagiculeCircuit.WireData wire1 = new MagiculeCircuit.WireData(triggerNode.id, 0, addBarrier.id, 0, false);
        MagiculeCircuit.WireData wire2 = new MagiculeCircuit.WireData(rateNode.id, 0, addBarrier.id, 1, true);

        circuit.addWire(wire1);
        circuit.addWire(wire2);

        circuit.setNodeParam(rateNode.id, "value", 0.05);

        List<UUID> nodes = new ArrayList<>();
        nodes.add(triggerNode.id);
        nodes.add(rateNode.id);
        nodes.add(addBarrier.id);

        return nodes;
    }
}
