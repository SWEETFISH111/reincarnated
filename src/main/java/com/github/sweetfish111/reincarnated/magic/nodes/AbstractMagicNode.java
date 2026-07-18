package com.github.sweetfish111.reincarnated.magic.nodes;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import net.minecraft.core.BlockPos;
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
            return link.sourcceNode().getOutputData(link.sourcePortIndex(), context);
        }
        return null;
    }
    protected Vec3 pullVector3(int myInputPortIndex, MagicContext context){
        Object rawData = pullData(myInputPortIndex, context);
        if(rawData == null){
            return Vec3.ZERO;
        }
        if(rawData instanceof Vec3 vec){
            return vec;
        }
        if(rawData instanceof BlockPos pos){
            return Vec3.atBottomCenterOf(pos);
        }
        return null;
    }

    protected net.minecraft.core.BlockPos pullBlockPos(int myInputPortIndex, MagicContext context) {
        Object rawData = pullData(myInputPortIndex, context);
        if (rawData == null) {
            return net.minecraft.core.BlockPos.ZERO;
        }

        // 1. もし最初から BlockPos 型ならそのままキャスト
        if (rawData instanceof net.minecraft.core.BlockPos pos) {
            return pos;
        }

        // 2. 【ここが解決のキモ】もし Vec3 型なら、四捨五入（または切り捨て）して BlockPos に変換！
        if (rawData instanceof net.minecraft.world.phys.Vec3 vec) {
            return net.minecraft.core.BlockPos.containing(vec.x, vec.y, vec.z);
            // 💡 マイクラの標準メソッド。vec.x などを BlockPos に安全に丸めてくれます
        }

        return net.minecraft.core.BlockPos.ZERO;
    }

    protected  void pushExecute(int myOuputPortIndex, MagicContext context){
        MagicNode nextNode = executeOutputs.get(myOuputPortIndex);
        if(nextNode != null){
            nextNode.execute(context);
        }
    }

    protected record DataLink(MagicNode sourcceNode, int sourcePortIndex){}
}
