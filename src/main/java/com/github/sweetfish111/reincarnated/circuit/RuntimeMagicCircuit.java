package com.github.sweetfish111.reincarnated.circuit;

import com.github.sweetfish111.reincarnated.event.CalculationCapacityOverException;
import com.github.sweetfish111.reincarnated.event.MasoShortageException;
import com.github.sweetfish111.reincarnated.magic.caster.IMagicCaster;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.reincarnated;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RuntimeMagicCircuit {
    private final IMagicCaster caster;
    private final CompiledCircuitGraph graph;

    public RuntimeMagicCircuit(IMagicCaster caster, CompiledCircuitGraph graph) {
        this.caster = caster;
        this.graph = graph;
    }

    public AbstractMagicNode getInstancedNode(UUID id){ return graph.getInstancedNodes().get(id); }
    public Map<UUID, AbstractMagicNode> getInstancedNodes(){ return graph.getInstancedNodes(); }
    public IMagicCaster getCaster(){ return this.caster; }

    public void start(MagicContext context) {
        for (AbstractMagicNode startNode : graph.getStartNodes()) {
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
            caster.getCasterLevel().explode(null, caster.getCasterPosition().x, caster.getCasterPosition().y, caster.getCasterPosition().z, 10.0f, Level.ExplosionInteraction.TNT);
        } catch (MasoShortageException m) {
            if (caster.getCasterEntity() instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.literal("《告》個体名" + player.getName().getString() + "の魔素残量が低下。術式を維持できません"));
            }
        }catch (NullPointerException n){
            if(caster.getCasterEntity() == null){
                reincarnated.LOGGER.info("ブロックエンティティに視線や目の位置を要求したためエラーが発生しました。術式は不発に終わります");
            }
        }
    }

    // 途中（ディレイや投射物の着弾など）から特定のノードを再開させる
    public void resumeNode(UUID nodeId, MagicContext context, int portIndex) {
        AbstractMagicNode node = graph.getInstancedNodes().get(nodeId);
        if (node != null) {
            node.executeOutputPort(portIndex, context);
        }
    }

}

