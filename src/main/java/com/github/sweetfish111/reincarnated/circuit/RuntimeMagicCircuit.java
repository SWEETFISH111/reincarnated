package com.github.sweetfish111.reincarnated.circuit;

import com.github.sweetfish111.reincarnated.event.CalculationCapacityOverException;
import com.github.sweetfish111.reincarnated.event.MasoShortageException;
import com.github.sweetfish111.reincarnated.magic.compiler.MagicCompiler;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.magic.nodes.MagicNode;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RuntimeMagicCircuit {
    private final ServerPlayer player;
    private final Map<UUID, AbstractMagicNode> instancedNodes; // 実体ノードのマップ
    private final Set<AbstractMagicNode> startNodes = new HashSet<>();                   // 起点となるノード

    public RuntimeMagicCircuit(ServerPlayer player, Map<UUID, AbstractMagicNode> instancedNodes, Set<AbstractMagicNode> startNodes) {
        this.player = player;
        this.instancedNodes = instancedNodes;
        this.startNodes.addAll(startNodes);
    }

    public AbstractMagicNode getInstancedNode(UUID id){return this.instancedNodes.get(id);}
    public Map<UUID, AbstractMagicNode> getInstancedNodes(){return this.instancedNodes;}
    public ServerPlayer getCaster(){return this.player;}

    // 魔法の発動（実行開始）
    public void execute(MagicContext context) {
        try {
            for (AbstractMagicNode startNode : startNodes) {
                startNode.execute(context);
            }
        } catch (CalculationCapacityOverException c) {
            context.getCaster().sendSystemMessage(Component.literal("《告》個体名" + context.getCaster().getName() + "の演算容量が限界を超過。術式暴走が発生"));
        } catch (MasoShortageException m) {
            context.getCaster().sendSystemMessage(Component.literal("《告》個体名" + context.getCaster().getName().getString() + "の魔素残量が低下。術式を維持できません"));
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

