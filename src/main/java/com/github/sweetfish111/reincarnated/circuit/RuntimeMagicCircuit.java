package com.github.sweetfish111.reincarnated.circuit;

import com.github.sweetfish111.reincarnated.event.CalculationCapacityOverException;
import com.github.sweetfish111.reincarnated.event.MasoShortageException;
import com.github.sweetfish111.reincarnated.magic.caster.IMagicCaster;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RuntimeMagicCircuit {
    private final IMagicCaster caster;
    private final Map<UUID, AbstractMagicNode> instancedNodes; // 実体ノードのマップ
    private final Set<AbstractMagicNode> startNodes = new HashSet<>();                   // 起点となるノード

    public RuntimeMagicCircuit(IMagicCaster caster, Map<UUID, AbstractMagicNode> instancedNodes, Set<AbstractMagicNode> startNodes) {
        this.caster = caster;
        this.instancedNodes = instancedNodes;
        this.startNodes.addAll(startNodes);
    }

    public AbstractMagicNode getInstancedNode(UUID id){return this.instancedNodes.get(id);}
    public Map<UUID, AbstractMagicNode> getInstancedNodes(){return this.instancedNodes;}
    public IMagicCaster getCaster(){return this.caster;}

    // 魔法の発動（実行開始）
    public void start(MagicContext context) {
        for(AbstractMagicNode startNode : startNodes){
            executeNode(caster, startNode.getId(), context);
        }
    }

    public static void executeNode(IMagicCaster caster, UUID nextNodeId, MagicContext context){
        AbstractMagicNode nextNode = context.getRuntimeCircuit().getInstancedNode(nextNodeId);
        try {
            nextNode.execute(context);
        } catch (CalculationCapacityOverException c) {
            if (caster.getCasterEntity() instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.literal("《告》個体名" + player.getName() + "の演算容量が限界を超過。術式暴走が発生"));
            }
            caster.getCasterEntity().level().explode(caster.getCasterEntity(), caster.getCasterEntity().getX(), caster.getCasterEntity().getY(), caster.getCasterEntity().getZ(), 10.0f, Level.ExplosionInteraction.TNT);
        } catch (MasoShortageException m) {
            if (caster.getCasterEntity() instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.literal("《告》個体名" + player.getName().getString() + "の魔素残量が低下。術式を維持できません"));
            }
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

