package com.github.sweetfish111.reincarnated.magic.skill.unique;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.circuit.MagiculeNodeType;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Hoarder {

    public static UUID getHoarder(MagiculeCircuit circuit) {

        MagiculeCircuit.NodeData triggerNode = new MagiculeCircuit.NodeData(UUID.randomUUID(), MagiculeNodeType.ON_OVERCHARGE, 100, 100);
        MagiculeCircuit.NodeData numberNode = new MagiculeCircuit.NodeData(UUID.randomUUID(), MagiculeNodeType.NUMBER, 150, 100);
        MagiculeCircuit.NodeData absorptionNode = new MagiculeCircuit.NodeData(UUID.randomUUID(), MagiculeNodeType.ABSORPTION, 200, 100);

        circuit.addNode(triggerNode);
        circuit.addNode(numberNode);
        circuit.addNode(absorptionNode);

        MagiculeCircuit.WireData wire1 = new MagiculeCircuit.WireData(triggerNode.id, 0, absorptionNode.id, 0, false);
        MagiculeCircuit.WireData wire2 = new MagiculeCircuit.WireData(numberNode.id, 0, absorptionNode.id, 1, true);

        circuit.addWire(wire1);
        circuit.addWire(wire2);

        circuit.setNodeParam(numberNode.id, "value", 1.0);

        List<UUID> nodes = new ArrayList<>();
        nodes.add(triggerNode.id);
        nodes.add(numberNode.id);
        nodes.add(absorptionNode.id);

        nodes.addAll(BarrierSkillCircuit.getBarrierSkillCircuit(circuit));


        return circuit.collapseNodes(nodes, Component.translatable("name.reincarnated.uniqueSkill.hoarder").getString());
    }
}
