package com.github.sweetfish111.reincarnated.magic.nodes;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractMagicNode implements MagicNode{
    protected final Map<Integer, MagicNode> executeOutputs = new HashMap<>();
    protected final Map<Integer, DataLink> dataInputs = new HashMap<>();
    protected UUID id;

    public AbstractMagicNode(){
        this.id = UUID.randomUUID();
    }
    public AbstractMagicNode(UUID id){
        this.id = id;
    }
    @Override
    public void connectTo(int sourcePortIndex, MagicNode targetNode, int targetPortIndex, boolean isDataFlow) {
        System.out.println("[haisen]source" + this.getClass().getSimpleName() + "[Pin:" + sourcePortIndex + "] -> target:" + targetNode.getClass().getSimpleName() + "[Pin:" + targetPortIndex + "](DataFlow:" + isDataFlow + ")");

        if(isDataFlow){
            if(targetNode instanceof AbstractMagicNode){
                ((AbstractMagicNode) targetNode).dataInputs.put(targetPortIndex, new DataLink(this, sourcePortIndex));
            }
        }else{
            this.executeOutputs.put(sourcePortIndex, targetNode);
        }
    }

    protected Object pullData(int myInputPortIndex, MagicContext context){
        DataLink link = dataInputs.get(myInputPortIndex);
        if(link != null){
            return link.sourceNode().getOutputData(link.sourcePortIndex(), context);
        }
        return null;
    }
    protected Vec3 pullVector3(int myInputPortIndex, MagicContext context){
        Object rawData = pullData(myInputPortIndex, context);
        return switch (rawData) {
            case null -> Vec3.ZERO;
            case Vec3 vec -> vec;
            case BlockPos pos -> Vec3.atBottomCenterOf(pos);
            default -> null;
        };
    }
    protected Entity pullEntity(int myInputPortIndex, MagicContext context){
        Object rawData = pullData(myInputPortIndex, context);
        return (Entity) rawData;
    }

    protected  void pushExecute(MagicContext context){
        MagicNode nextNode = executeOutputs.get(0);
        if(nextNode != null){
            nextNode.execute(context);
        }
    }

    protected record DataLink(MagicNode sourceNode, int sourcePortIndex){}
}
