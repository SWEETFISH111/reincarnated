package com.github.sweetfish111.reincarnated.magic.skill.unique;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.circuit.MagiculeNodeType;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Scavenger {
    public static UUID getScavenger(MagiculeCircuit circuit) {

        MagiculeCircuit.NodeData triggerNode = new MagiculeCircuit.NodeData(UUID.randomUUID(), MagiculeNodeType.ON_EAT, 100, 100);
        MagiculeCircuit.NodeData conversNode = new MagiculeCircuit.NodeData(UUID.randomUUID(), MagiculeNodeType.COMBERS_SATIETY_TO_MASO, 150, 100);
        MagiculeCircuit.NodeData addMaso = new MagiculeCircuit.NodeData(UUID.randomUUID(), MagiculeNodeType.ADD_MASO, 200, 100);

        circuit.addNode(triggerNode);
        circuit.addNode(conversNode);
        circuit.addNode(addMaso);

        MagiculeCircuit.WireData wire1 = new MagiculeCircuit.WireData(triggerNode.id, 0, addMaso.id, 0, false);
        MagiculeCircuit.WireData wire2 = new MagiculeCircuit.WireData(triggerNode.id, 1, conversNode.id, 0, true);
        MagiculeCircuit.WireData wire3 = new MagiculeCircuit.WireData(conversNode.id, 0, addMaso.id, 1, true);

        circuit.addWire(wire1);
        circuit.addWire(wire2);
        circuit.addWire(wire3);

        List<UUID> nodes = new ArrayList<>();
        nodes.add(triggerNode.id);
        nodes.add(conversNode.id);
        nodes.add(addMaso.id);

        nodes.addAll(BarrierSkillCircuit.getBarrierSkillCircuit(circuit));

        return circuit.collapseNodes(nodes, Component.translatable("name.reincarnated.uniqueSkill.scavenger").getString());
    }
}
