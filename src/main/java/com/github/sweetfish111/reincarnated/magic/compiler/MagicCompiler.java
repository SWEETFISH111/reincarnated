package com.github.sweetfish111.reincarnated.magic.compiler;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.circuit.PortDataType;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.*;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MagicCompiler {
    public static void compileAndExecute(MagiculeCircuit circuit, ServerPlayer caster, String triggerType){
        System.out.println("compiler on. trigger:"+ triggerType);

        Map<UUID, MagicNode> instancedNodes = new HashMap<>();
        for (MagiculeCircuit.NodeData data : circuit.getNodes()){
            System.out.println("compiler node test:" + data.type.getId());

            MagicNode actualNode = createNodeInstance(data.type.getId(), data.id);
            if(actualNode != null){
                instancedNodes.put(data.id, actualNode);
            }
        }

        System.out.println("compiler node test finished. start wire fase");

        for (MagiculeCircuit.WireData wire : circuit.getWires()){
            System.out.println("compiler wire test:" + wire.sourceId + "->" + wire.targetId);

            MagicNode sourceNode = instancedNodes.get(wire.sourceId);
            MagicNode targetNode = instancedNodes.get(wire.targetId);
            if(sourceNode != null && targetNode != null){
                sourceNode.connectTo(wire.sourcePortIndex, targetNode, wire.targetPortIndex, wire.isDataFlow);
            }
        }

        System.out.println("compiler wire fase finished. start hakka fase");

        MagicContext context = new MagicContext(caster, circuit);
        boolean isFired = false;

        for (MagiculeCircuit.NodeData data : circuit.getNodes()){
            if(data.type.getId().equals(triggerType)){
                MagicNode startNode = instancedNodes.get(data.id);
                if(startNode != null){
                    System.out.println("start node" + triggerType + "found");
                    startNode.execute(context);
                    isFired = true;
                    break;
                }
            }
        }
    }



    public static MagicNode createNodeInstance(String typeId, UUID nodeId){
        switch (typeId){
            case "event_key_1":return new EventKeyOneNode();
            case "lightning":return new SummonLightningNode();
            case "get_look_target":return new GetLookTargetNode();
            case "explosion":return new ExplosionNode();
            case "caster_pos":return new CasterPosNode();
            case "offset":return new OffsetNode();
            case "get_look_forward":return new GetLookForwardNode();
            case "number":return new NumberNode(nodeId);
            default : return null;
        }
    }
}
