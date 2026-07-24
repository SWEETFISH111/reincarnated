package com.github.sweetfish111.reincarnated.magic.nodes;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public abstract class AbstractMagicNode implements MagicNode{
    protected final Map<Integer, MagicNode> executeOutputs = new HashMap<>();
    protected final Map<Integer, DataLink> dataInputs = new HashMap<>();
    protected final Map<Integer, List<MagicNode>> outputConnections = new HashMap<>();
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
            this.outputConnections
                    .computeIfAbsent(sourcePortIndex, k -> new ArrayList<>())
                    .add(targetNode);
        }
    }

    public Map<Integer, List<MagicNode>> getOutputConnections(){return outputConnections;}
    public MagicNode getNextNode(int portIndex){
        return outputConnections.get(portIndex).getFirst();
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
    protected double pullDouble(int myInputPortIndex, MagicContext context){
        Object rawData = pullData(myInputPortIndex, context);
        if (rawData instanceof Number num) {
            return num.doubleValue();
        }
        return 0.0;
    }
    protected boolean pullBoolean(int myInputPortIndex, MagicContext context){
        Object rawData = pullData(myInputPortIndex, context);
        if (rawData instanceof Boolean val) {
            return val.booleanValue();
        }
        return false;
    }

    protected void executeOutputPort(int outputPortIndex, MagicContext context){
        List<MagicNode> nextNodes = outputConnections.get(outputPortIndex);
        if(nextNodes != null){
            for(MagicNode node : nextNodes){
                node.execute(context);
            }
        }

    }
    protected void pushExecute(MagicContext context){
        executeOutputPort(0, context);
    }
    protected void pushExecute(int outputPortIndex, MagicContext context){
        executeOutputPort(outputPortIndex, context);
    }

    protected record DataLink(MagicNode sourceNode, int sourcePortIndex){}
}
