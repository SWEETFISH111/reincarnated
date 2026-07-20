package com.github.sweetfish111.reincarnated.magic.compiler;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.*;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MagicCompiler {
    public static void compileAndExecute(MagiculeCircuit circuit, ServerPlayer caster, String triggerType){
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
            default : return null;
        }
    }
}
