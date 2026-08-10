package com.github.sweetfish111.reincarnated.magic.nodes;

import com.github.sweetfish111.reincarnated.event.MasoShortageException;
import com.github.sweetfish111.reincarnated.magic.*;
import com.github.sweetfish111.reincarnated.magic.caster.IMagicCaster;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.*;

public abstract class AbstractMagicNode implements MagicNode{
    protected final Map<Integer, MagicNode> executeOutputs = new HashMap<>();
    protected final Map<Integer, DataLink> dataInputs = new HashMap<>();
    protected final Map<Integer, List<MagicNode>> outputConnections = new HashMap<>();
    protected UUID id;
    protected float masoCost;
    protected boolean isTrigger = false;
    protected IMagicCaster caster;
    protected Map<String, Object> eventData = null;
    protected String triggerType = null;

    public AbstractMagicNode(UUID id){
        masoCost = 0.0f;
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
        List<MagicNode> connections = this.outputConnections.get(portIndex);
        if (connections == null || connections.isEmpty()) {
            return null;
        }
        return connections.getFirst();
    }
    public boolean isTrigger(){return this.isTrigger;}
    public String getTriggerType(){return this.triggerType;}
    public void setEventData(Map<String, Object> data){this.eventData = data;}


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
    protected XpAmount pullXp(int myInputPortIndex, MagicContext context){
        Object rawData = pullData(myInputPortIndex,context);
        if(rawData instanceof XpAmount xp){
            return xp;
        }
        return new XpAmount(0);
    }
    protected MasoAmount pullMaso(int myInputPortIndex, MagicContext context){
        Object rawData = pullData(myInputPortIndex,context);
        if(rawData instanceof MasoAmount maso){
            return maso;
        }
        return new MasoAmount(0);
    }
    protected KillScoreAmount pullKillScore(int myInputPortIndex, MagicContext context){
        Object rawData = pullData(myInputPortIndex,context);
        if(rawData instanceof KillScoreAmount kscore){
            return kscore;
        }
        return new KillScoreAmount(0);
    }
    protected PowerGapAmount pullPowerGap(int myInputPortIndex, MagicContext context){
        Object rawData = pullData(myInputPortIndex, context);
        if(rawData instanceof PowerGapAmount pGap){
            return pGap;
        }
        return new PowerGapAmount(0);
    }
    protected SatietyAmount pullSatiety(int myInputPortIndex, MagicContext context){
        Object rawData = pullData(myInputPortIndex, context);
        if(rawData instanceof SatietyAmount satietyAmount){
            return satietyAmount;
        }
        return new SatietyAmount(0);
    }

    public void executeOutputPort(int outputPortIndex, MagicContext context){
        List<MagicNode> nextNodes = outputConnections.get(outputPortIndex);
        if(nextNodes != null){
            for(MagicNode node : nextNodes){
                node.execute(context);
            }
        }
    }

    @Override
    public void execute(MagicContext context) {
        consumeMaso(masoCost, context.getCaster());
        context.incrementAndCheck();
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        context.incrementAndCheck();
        return null;
    }

    public UUID getId(){return this.id;}

    public void pushExecute(MagicContext context){
        executeOutputPort(0, context);
    }
    public void pushExecute(int outputPortIndex, MagicContext context){
        executeOutputPort(outputPortIndex, context);
    }

    protected void consumeMaso(float masoCost, IMagicCaster caster){
        if(caster.getMasoAmount() >= masoCost){
            caster.consumeMaso(masoCost);
        }else{
            throw new MasoShortageException(masoCost, caster.getMasoAmount());
        }
        System.out.println("AbstractMagicNode:cost_" + masoCost + " current_" + caster.getMasoAmount());
    }

    public static void ensureMaxAbsorption(ServerPlayer player, float needed) {
        AttributeInstance maxAbsorption = player.getAttribute(Attributes.MAX_ABSORPTION);
        if (maxAbsorption != null && maxAbsorption.getBaseValue() < needed) {
            maxAbsorption.setBaseValue(needed);
        }
    }

    protected record DataLink(MagicNode sourceNode, int sourcePortIndex){}
}
