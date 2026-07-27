package com.github.sweetfish111.reincarnated.magic.compiler;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.circuit.MagiculeNodeType;
import com.github.sweetfish111.reincarnated.magic.nodes.sensor.GetLookForwardNode;
import com.github.sweetfish111.reincarnated.magic.nodes.sensor.GetLookTargetNode;
import com.github.sweetfish111.reincarnated.magic.nodes.sensor.ReturnCaster;
import com.github.sweetfish111.reincarnated.magic.nodes.action.ExplosionNode;
import com.github.sweetfish111.reincarnated.magic.nodes.action.SummonLightningNode;
import com.github.sweetfish111.reincarnated.magic.nodes.math.*;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.*;
import com.github.sweetfish111.reincarnated.magic.nodes.conversion.CombersLookDirection;
import com.github.sweetfish111.reincarnated.magic.nodes.conversion.CombersTargetPos;
import com.github.sweetfish111.reincarnated.magic.nodes.conversion.OffsetNode;
import com.github.sweetfish111.reincarnated.magic.nodes.control.IfNode;
import com.github.sweetfish111.reincarnated.magic.nodes.control.RepeatNode;
import com.github.sweetfish111.reincarnated.magic.nodes.trigger.EventKeyOneNode;
import com.github.sweetfish111.reincarnated.magic.nodes.value.BooleanNode;
import com.github.sweetfish111.reincarnated.magic.nodes.value.NumberNode;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class MagicCompiler {
    private static void compileNodes(MagiculeCircuit circuit, Map<UUID, MagicNode> instancedNodes){
        for (MagiculeCircuit.NodeData data : circuit.getNodes()){
            MagicNode actualNode = createNodeInstance(data.type.getId(), data.id);
            if(actualNode != null){
                instancedNodes.put(data.id, actualNode);
            }
        }
    }

    private static void compileCompoundNodes(MagiculeCircuit circuit, Map<UUID, MagicNode> instancedNodes, List<MagiculeCircuit.WireData> allWires){
        for(MagiculeCircuit.CompoundNodeData data : circuit.getCompoundNodes()){
            MagiculeCircuit innerCircuit = new MagiculeCircuit();

            innerCircuit.setNodes(data.innerNodes);
            compileNodes(innerCircuit, instancedNodes);

            innerCircuit.setCompoundNodes(data.innerCompoundNodes);
            compileCompoundNodes(innerCircuit, instancedNodes, allWires);

            allWires.addAll(data.innerWires);
        }
    }

    private static List<MagiculeCircuit.NodeData> getInnerProxys(MagiculeCircuit.CompoundNodeData target, MagiculeNodeType proxyType){
        if(proxyType != MagiculeNodeType.INPUT_PROXY && proxyType != MagiculeNodeType.OUTPUT_PROXY){return null;}

        List<MagiculeCircuit.NodeData> result = new ArrayList<>();
        for(MagiculeCircuit.NodeData node : target.innerNodes){
            if(node.type == proxyType){
                if(target.innerNodeParameters.get(node.id).get("value") instanceof Double d)
               result.add(d.intValue(), node);
            }
        }
        return result;
    }

    private static void compileWires(MagiculeCircuit circuit, Map<UUID, MagicNode> instancedNodes, List<MagiculeCircuit.WireData> allWires){
        for (MagiculeCircuit.WireData wire : allWires){
            MagicNode sourceNode = instancedNodes.get(wire.sourceId);
            MagicNode targetNode = instancedNodes.get(wire.targetId);

            MagiculeCircuit.CompoundNodeData sourceCNode = circuit.getCNode(wire.sourceId);
            MagiculeCircuit.CompoundNodeData targetCNode = circuit.getCNode(wire.targetId);

            int sourcePortIndex = wire.sourcePortIndex;
            int targetPortIndex = wire.targetPortIndex;

            if(targetCNode != null){
                List<MagiculeCircuit.NodeData> inputProxys = getInnerProxys(targetCNode, MagiculeNodeType.INPUT_PROXY);
                for(MagiculeCircuit.WireData innerWire : targetCNode.innerWires){
                    if(innerWire.sourceId.equals(inputProxys.get(targetPortIndex).id)){
                        targetNode = instancedNodes.get(innerWire.targetId);
                        targetPortIndex = innerWire.targetPortIndex;
                    }
                }
            }
            if(sourceCNode != null){
                List<MagiculeCircuit.NodeData> outputProxys = getInnerProxys(sourceCNode, MagiculeNodeType.OUTPUT_PROXY);
                for(MagiculeCircuit.WireData innerWire : targetCNode.innerWires){
                    if(innerWire.targetId.equals(outputProxys.get(sourcePortIndex).id)){
                        sourceNode = instancedNodes.get(innerWire.targetId);
                        sourcePortIndex = innerWire.sourcePortIndex;
                    }
                }
            }
            if(sourceNode != null && targetNode != null){
                sourceNode.connectTo(wire.sourcePortIndex, targetNode, wire.targetPortIndex, wire.isDataFlow);
            }
        }
    }

    public static void compileAndExecute(MagiculeCircuit circuit, ServerPlayer caster, String triggerType){
        Map<UUID, MagicNode> instancedNodes = new HashMap<>();
        List<MagiculeCircuit.WireData> allWires = new ArrayList<>();
        allWires.addAll(circuit.getWires());
        compileNodes(circuit, instancedNodes);
        compileCompoundNodes(circuit, instancedNodes, allWires);
        compileWires(circuit, instancedNodes, allWires);

        MagicContext context = new MagicContext(caster, circuit);

        for (MagiculeCircuit.NodeData data : circuit.getNodes()){
            if(data.type.getId().equals(triggerType)){
                MagicNode startNode = instancedNodes.get(data.id);
                if(startNode != null){
                    startNode.execute(context);
                    break;
                }
            }
        }
    }

    public static MagicNode resolveNodeInstance(MagiculeCircuit circuit, UUID nodeId){
        Map<UUID, MagicNode> instancedNodes = new HashMap<>();
        for (MagiculeCircuit.NodeData data : circuit.getNodes()){
            MagicNode actualNode = createNodeInstance(data.type.getId(), data.id);
            if(actualNode != null){
                instancedNodes.put(data.id, actualNode);
            }
        }

        for (MagiculeCircuit.WireData wire : circuit.getWires()){
            MagicNode sourceNode = instancedNodes.get(wire.sourceId);
            MagicNode targetNode = instancedNodes.get(wire.targetId);
            if(sourceNode != null && targetNode != null){
                sourceNode.connectTo(wire.sourcePortIndex, targetNode, wire.targetPortIndex, wire.isDataFlow);
            }
        }

        return instancedNodes.get(nodeId);
    }




    public static MagicNode createNodeInstance(String typeId, UUID nodeId){
        switch (typeId){
            case "event_key_1":return new EventKeyOneNode();
            case "lightning":return new SummonLightningNode();
            case "get_look_target":return new GetLookTargetNode();
            case "explosion":return new ExplosionNode();
            case "caster_pos":return new ReturnCaster();
            case "offset":return new OffsetNode(nodeId);
            case "get_look_forward":return new GetLookForwardNode(nodeId);
            case "number":return new NumberNode(nodeId);
            case "combers_target_pos":return new CombersTargetPos();
            case "combers_look_direction":return new CombersLookDirection();
            case "if":return new IfNode(nodeId);
            case "boolean":return new BooleanNode(nodeId);
            case "repeat":return new RepeatNode(nodeId);
            case "add":return new AddNode();
            case "subtract":return new SubtractNode();
            case "multiply":return new MultiplyNode();
            case "divide":return new DivideNode();
            case "modulo":return new ModuloNode();
            case "equal":return new EqualsNode();
            case "not":return new NotNode();
            case "or":return new OrNode();
            case "and":return new AndNode();
            case "greater_than":return new GreagerThanNode();
            case "greater_or_equal":return new GreaterOrEqualNode();
            case "less_than":return new LessThanNode();
            case "less_or_equal":return new LessOrEqualNode();
            case "shoot_projectile":return new ShootProjectileNode();
            default : return null;
        }
    }
}
