package com.github.sweetfish111.reincarnated.circuit;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.magic.nodes.MagicNode;

import java.util.Map;
import java.util.UUID;

public class RuntimeMagicCircuit {
    private final UUID casterId;
    private final Map<UUID, AbstractMagicNode> instancedNodes; // 実体ノードのマップ
    private final AbstractMagicNode startNode;                   // 起点となるノード

    public RuntimeMagicCircuit(UUID casterId, Map<UUID, AbstractMagicNode> instancedNodes, AbstractMagicNode startNode) {
        this.casterId = casterId;
        this.instancedNodes = instancedNodes;
        this.startNode = startNode;
    }

    // 魔法の発動（実行開始）
    public void execute(MagicContext context) {
        if (startNode != null) {
            startNode.execute(context);
        }
    }

    // 途中（ディレイや投射物の着弾など）から特定のノードを再開させる
    public void resumeNode(UUID nodeId, MagicContext context, int portIndex) {
        AbstractMagicNode node = instancedNodes.get(nodeId);
        if (node != null) {
            node.executeOutputPort(portIndex, context);
        }
    }
}

