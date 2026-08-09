package com.github.sweetfish111.reincarnated.player;


import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.circuit.MagiculeNodeType;
import com.github.sweetfish111.reincarnated.event.PlayerUniqueSkillAcquiredEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DefaultCircuitBuilder {

    /**
     * スキルタブの初期回路（貪欲者）を構築する
     */
    public static UUID buildDefaultSkillCircuit(MagiculeCircuit circuit) {

        // 1. 起点ノード（OnXpPickupNode）をキャンバスの中心付近（例: x=100, y=100）に生成
        MagiculeCircuit.NodeData xpNodeData = new MagiculeCircuit.NodeData(UUID.randomUUID(), MagiculeNodeType.ON_XP_PICKUP, 100, 100);
        MagiculeCircuit.NodeData xpToMaso = new MagiculeCircuit.NodeData(UUID.randomUUID(), MagiculeNodeType.CONBERS_XP_TO_MASO, 150, 100);
        MagiculeCircuit.NodeData addMaso = new MagiculeCircuit.NodeData(UUID.randomUUID(), MagiculeNodeType.ADD_MASO, 200, 100);

        // 回路のノードマップに追加
        circuit.addNode(xpNodeData);
        circuit.addNode(xpToMaso);
        circuit.addNode(addMaso);

        List<MagiculeCircuit.WireData> wires = new ArrayList<>();
        MagiculeCircuit.WireData wire1 = new MagiculeCircuit.WireData(xpNodeData.id, 0, addMaso.id, 0, false);
        MagiculeCircuit.WireData wire2 = new MagiculeCircuit.WireData(xpNodeData.id, 1, xpToMaso.id, 0, true);
        MagiculeCircuit.WireData wire3 = new MagiculeCircuit.WireData(xpToMaso.id, 0, addMaso.id, 1, true);

        wires.add(wire1);
        wires.add(wire2);
        wires.add(wire3);

        circuit.setWires(wires);

        List<UUID> nodes = new ArrayList<>();
        nodes.add(xpNodeData.id);
        nodes.add(xpToMaso.id);
        nodes.add(addMaso.id);

        UUID greedyId = circuit.collapseNodes(nodes, Component.translatable("name.reincarnated.uniqueSkill.greedy").getString());

        return greedyId;
    }
}