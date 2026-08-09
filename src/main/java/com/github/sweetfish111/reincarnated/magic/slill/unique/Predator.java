package com.github.sweetfish111.reincarnated.magic.slill.unique;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.circuit.MagiculeNodeType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Predator {
    MagiculeCircuit circuit = new MagiculeCircuit();

    public static void getPredator(MagiculeCircuit parentCircuit){
        List<MagiculeCircuit.NodeData> nodes = new ArrayList<>();
        nodes.add(new MagiculeCircuit.NodeData(UUID.randomUUID(), MagiculeNodeType.ON_KILL, 10, 10));
        nodes.add(new MagiculeCircuit.NodeData(UUID.randomUUID(), MagiculeNodeType.CONBERS_XP_TO_MASO, 10 + 60 + 10, 20));
        nodes.add(new MagiculeCircuit.NodeData(UUID.randomUUID(), MagiculeNodeType.ADD_MASO, 80 + 60 + 10, 10));
    }
}
