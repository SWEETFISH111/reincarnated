package com.github.sweetfish111.reincarnated.magic.compiler;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.circuit.MagiculeNodeType;
import com.github.sweetfish111.reincarnated.circuit.RuntimeMagicCircuit;
import com.github.sweetfish111.reincarnated.magic.nodes.action.DamageNode;
import com.github.sweetfish111.reincarnated.magic.nodes.action.HealingNode;
import com.github.sweetfish111.reincarnated.magic.nodes.control.DelayNode;
import com.github.sweetfish111.reincarnated.magic.nodes.control.toggleNode;
import com.github.sweetfish111.reincarnated.magic.nodes.sensor.GetLookForwardNode;
import com.github.sweetfish111.reincarnated.magic.nodes.sensor.GetLookTargetNode;
import com.github.sweetfish111.reincarnated.magic.nodes.sensor.ReturnCaster;
import com.github.sweetfish111.reincarnated.magic.nodes.action.ExplosionNode;
import com.github.sweetfish111.reincarnated.magic.nodes.action.SummonLightningNode;
import com.github.sweetfish111.reincarnated.magic.nodes.math.*;
import com.github.sweetfish111.reincarnated.magic.nodes.*;
import com.github.sweetfish111.reincarnated.magic.nodes.conversion.CombersLookDirection;
import com.github.sweetfish111.reincarnated.magic.nodes.conversion.CombersTargetPos;
import com.github.sweetfish111.reincarnated.magic.nodes.conversion.OffsetNode;
import com.github.sweetfish111.reincarnated.magic.nodes.control.IfNode;
import com.github.sweetfish111.reincarnated.magic.nodes.control.RepeatNode;
import com.github.sweetfish111.reincarnated.magic.nodes.trigger.EventKeyOneNode;
import com.github.sweetfish111.reincarnated.magic.slill.node.trigger.onTickNode;
import com.github.sweetfish111.reincarnated.magic.nodes.value.BooleanNode;
import com.github.sweetfish111.reincarnated.magic.nodes.value.NumberNode;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class MagicCompiler {

    private static void compileNodes(MagiculeCircuit circuit, Map<UUID, AbstractMagicNode> instancedNodes){
        if (circuit.getNodes() == null) return;
        for (MagiculeCircuit.NodeData data : circuit.getNodes()){
            MagicNode actualNode = createNodeInstance(data.type.getId(), data.id);
            if(actualNode != null){
                instancedNodes.put(data.id, (AbstractMagicNode) actualNode);
            }
        }
    }

    // 再帰的にすべての階層のノードをインスタンス化し、全階層のワイヤーをリストに回収する
    private static void compileCompoundNodes(MagiculeCircuit circuit, Map<UUID, AbstractMagicNode> instancedNodes, List<MagiculeCircuit.WireData> allWires){
        if (circuit.getCompoundNodes() == null) return;
        for(MagiculeCircuit.CompoundNodeData data : circuit.getCompoundNodes()){
            MagiculeCircuit innerCircuit = data.getCompoundCircuit();
            if (innerCircuit == null) continue;

            // ノード実体化
            compileNodes(innerCircuit, instancedNodes);

            // パラメータの復元
            if(data.getCompoundCircuit().getNodeParameters() != null){
                for(Map.Entry<UUID, Map<String, Object>> entry : data.getCompoundCircuit().getNodeParameters().entrySet()){
                    UUID nId = entry.getKey();
                    if (entry.getValue() != null) {
                        for(Map.Entry<String, Object> paramEntry : entry.getValue().entrySet()){
                            innerCircuit.setNodeParam(nId, paramEntry.getKey(), paramEntry.getValue());
                        }
                    }
                }
            }

            // さらに深い入れ子（再帰）
            compileCompoundNodes(innerCircuit, instancedNodes, allWires);

            // この階層のインナーワイヤーを全体リストに回収
            if(innerCircuit.getWires() != null){
                allWires.addAll(innerCircuit.getWires());
            }
        }
    }

    private static List<MagiculeCircuit.NodeData> getInnerProxys(MagiculeCircuit.CompoundNodeData target, MagiculeNodeType proxyType){
        if(target == null || target.getCompoundCircuit() == null || target.getCompoundCircuit().getNodes() == null) {
            return new ArrayList<>();
        }
        if(proxyType != MagiculeNodeType.INPUT_PROXY && proxyType != MagiculeNodeType.OUTPUT_PROXY){
            return new ArrayList<>();
        }

        List<MagiculeCircuit.NodeData> result = new ArrayList<>();
        Map<Double, MagiculeCircuit.NodeData> proxyIndex = new HashMap<>();
        List<Double> indexList = new ArrayList<>();

        for(MagiculeCircuit.NodeData node : target.getCompoundCircuit().getNodes()){
            if(node.type == proxyType){
                double val = 0.0;
                if(target.getCompoundCircuit().getNodeParameters() != null && target.getCompoundCircuit().getNodeParameters().get(node.id) != null){
                    Object obj = target.getCompoundCircuit().getNodeParameters().get(node.id).get("value");
                    if(obj instanceof Number n) {
                        val = n.doubleValue();
                    }
                }
                proxyIndex.put(val, node);
                indexList.add(val);
            }
        }
        Collections.sort(indexList);
        for(Double d : indexList){
            result.add(proxyIndex.get(d));
        }
        return result;
    }

    // 解決コンテキスト（どのノードがどのコンパウンド内にいるかを引くマップ）
    private static class WireResolutionContext {
        final Map<UUID, MagiculeCircuit.CompoundNodeData> compoundMap = new HashMap<>();
        final Map<UUID, MagiculeCircuit> circuitMap = new HashMap<>();

        void register(MagiculeCircuit circuit) {
            if (circuit.getCompoundNodes() == null) return;
            for (MagiculeCircuit.CompoundNodeData cData : circuit.getCompoundNodes()) {
                compoundMap.put(cData.id, cData);
                if (cData.getCompoundCircuit() != null) {
                    circuitMap.put(cData.id, cData.getCompoundCircuit());
                    register(cData.getCompoundCircuit());
                }
            }
        }
    }

    // プロキシやコンパウンドを透過して、最終的な「実体ノード」と「ポート番号」を割り出す再帰解決ロジック
    private static ResolvedEndpoint resolveEndpoint(UUID nodeId, int portIndex, boolean isTarget, WireResolutionContext ctx, Map<UUID, AbstractMagicNode> instancedNodes) {
        // 1. すでに実体ノードならそのまま返す
        if (instancedNodes.containsKey(nodeId)) {
            return new ResolvedEndpoint(instancedNodes.get(nodeId), portIndex);
        }

        // 2. コンパウンドノード自体、あるいはプロキシを内包するコンパウンドの場合
        MagiculeCircuit.CompoundNodeData cNode = ctx.compoundMap.get(nodeId);
        if (cNode != null) {
            MagiculeCircuit inner = cNode.getCompoundCircuit();
            if (inner != null && inner.getWires() != null) {
                if (isTarget) {
                    // ターゲット側：INPUT_PROXY を経由して中へ入る
                    List<MagiculeCircuit.NodeData> inputProxys = getInnerProxys(cNode, MagiculeNodeType.INPUT_PROXY);
                    if (portIndex >= 0 && portIndex < inputProxys.size()) {
                        UUID proxyId = inputProxys.get(portIndex).id;
                        for (MagiculeCircuit.WireData innerWire : inner.getWires()) {
                            if (innerWire.sourceId.equals(proxyId)) {
                                return resolveEndpoint(innerWire.targetId, innerWire.targetPortIndex, true, ctx, instancedNodes);
                            }
                        }
                    }
                } else {
                    // ソース側：OUTPUT_PROXY へ向かう
                    List<MagiculeCircuit.NodeData> outputProxys = getInnerProxys(cNode, MagiculeNodeType.OUTPUT_PROXY);
                    if (portIndex >= 0 && portIndex < outputProxys.size()) {
                        UUID proxyId = outputProxys.get(portIndex).id;
                        for (MagiculeCircuit.WireData innerWire : inner.getWires()) {
                            if (innerWire.targetId.equals(proxyId)) {
                                return resolveEndpoint(innerWire.sourceId, innerWire.sourcePortIndex, false, ctx, instancedNodes);
                            }
                        }
                    }
                }
            }
        }

        return new ResolvedEndpoint(null, portIndex);
    }

    private static class ResolvedEndpoint {
        final MagicNode node;
        final int portIndex;
        ResolvedEndpoint(MagicNode node, int portIndex) {
            this.node = node;
            this.portIndex = portIndex;
        }
    }

    private static void compileWires(MagiculeCircuit rootCircuit, Map<UUID, AbstractMagicNode> instancedNodes, List<MagiculeCircuit.WireData> allWires){
        if (allWires == null) return;

        WireResolutionContext ctx = new WireResolutionContext();
        ctx.register(rootCircuit);

        for (MagiculeCircuit.WireData wire : allWires){
            ResolvedEndpoint source = resolveEndpoint(wire.sourceId, wire.sourcePortIndex, false, ctx, instancedNodes);
            ResolvedEndpoint target = resolveEndpoint(wire.targetId, wire.targetPortIndex, true, ctx, instancedNodes);

            if(source.node != null && target.node != null){
                source.node.connectTo(source.portIndex, target.node, target.portIndex, wire.isDataFlow);
            }
        }
    }

    public static RuntimeMagicCircuit compileCircuit(ServerPlayer player, MagiculeCircuit circuit){
        Map<UUID, AbstractMagicNode> instancedNodes = new HashMap<>();
        List<MagiculeCircuit.WireData> allWires = new ArrayList<>();

        if (circuit.getWires() != null) {
            allWires.addAll(circuit.getWires());
        }

        compileNodes(circuit, instancedNodes);
        compileCompoundNodes(circuit, instancedNodes, allWires);
        compileWires(circuit, instancedNodes, allWires);
        AbstractMagicNode startNode = null;

        for(Map.Entry<UUID, AbstractMagicNode> entry : instancedNodes.entrySet()){
            if(entry.getValue().isTrigger()){
                startNode = entry.getValue();
            }
        }
        if(startNode != null){
            return new RuntimeMagicCircuit(player, instancedNodes, startNode);
        }
        return null;
    }

    public static MagicNode createNodeInstance(String typeId, UUID nodeId){
        switch (typeId){
            case "event_key_1":return new EventKeyOneNode(nodeId);
            case "lightning":return new SummonLightningNode(nodeId);
            case "get_look_target":return new GetLookTargetNode(nodeId);
            case "explosion":return new ExplosionNode(nodeId);
            case "caster_pos":return new ReturnCaster(nodeId);
            case "offset":return new OffsetNode(nodeId);
            case "get_look_forward":return new GetLookForwardNode(nodeId);
            case "number":return new NumberNode(nodeId);
            case "combers_target_pos":return new CombersTargetPos(nodeId);
            case "combers_look_direction":return new CombersLookDirection(nodeId);
            case "if":return new IfNode(nodeId);
            case "boolean":return new BooleanNode(nodeId);
            case "repeat":return new RepeatNode(nodeId);
            case "add":return new AddNode(nodeId);
            case "subtract":return new SubtractNode(nodeId);
            case "multiply":return new MultiplyNode(nodeId);
            case "divide":return new DivideNode(nodeId);
            case "modulo":return new ModuloNode(nodeId);
            case "equal":return new EqualsNode(nodeId);
            case "not":return new NotNode(nodeId);
            case "or":return new OrNode(nodeId);
            case "and":return new AndNode(nodeId);
            case "greater_than":return new GreagerThanNode(nodeId);
            case "greater_or_equal":return new GreaterOrEqualNode(nodeId);
            case "less_than":return new LessThanNode(nodeId);
            case "less_or_equal":return new LessOrEqualNode(nodeId);
            case "shoot_projectile":return new ShootProjectileNode(nodeId);
            case "damage":return new DamageNode(nodeId);
            case "healing":return new HealingNode(nodeId);
            case "delay":return new DelayNode(nodeId);
            case "on_tick":return new onTickNode(nodeId);
            case "toggle":return new toggleNode(nodeId);
            default : return null;
        }
    }
}