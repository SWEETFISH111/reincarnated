package com.github.sweetfish111.reincarnated.circuit;

import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CompiledCircuitGraph {
    private final Map<UUID, AbstractMagicNode> instancedNodes;
    private final Set<AbstractMagicNode> startNodes;

    public CompiledCircuitGraph(Map<UUID, AbstractMagicNode> instancedNodes, Set<AbstractMagicNode> startNodes) {
        this.instancedNodes = instancedNodes;
        this.startNodes = startNodes;
    }

    public Map<UUID, AbstractMagicNode> getInstancedNodes() { return instancedNodes; }
    public Set<AbstractMagicNode> getStartNodes() { return startNodes; }
}