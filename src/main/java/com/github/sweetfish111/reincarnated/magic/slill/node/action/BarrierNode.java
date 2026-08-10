package com.github.sweetfish111.reincarnated.magic.slill.node.action;

import com.github.sweetfish111.reincarnated.init.ModAttachments;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.player.PlayerMagicData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.UUID;

public class BarrierNode extends AbstractMagicNode {
    float BASECOST = 3;

    public BarrierNode(UUID id) {
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
        context.incrementAndCheck();

        PlayerMagicData magicData = null;
        float barrierPoint = (float) pullDouble(1, context);
        float currentMaso = context.getCaster().getMasoAmount();
        float maxBarrierPoint = 0.0f;
        float currentBarrierPoint = 0.0f;


        if(context.getCaster().getCasterEntity() instanceof ServerPlayer player){
            magicData = player.getData(ModAttachments.PLAYER_MAGIC_DATA);
            maxBarrierPoint = magicData.getMaxBarrierPoint();
            currentBarrierPoint = magicData.getBarrierPoint();
            if(maxBarrierPoint <= currentBarrierPoint)return;
        }
        if(currentMaso > barrierPoint * BASECOST){
            if(maxBarrierPoint >= barrierPoint + currentBarrierPoint){
                if(context.getCaster().getCasterEntity() instanceof ServerPlayer player){
                    player.getData(ModAttachments.PLAYER_MAGIC_DATA).setBarrierPoint(currentBarrierPoint + barrierPoint);
                    consumeMaso(BASECOST * barrierPoint, context.getCaster());
                }
            }else{
                barrierPoint = maxBarrierPoint - currentBarrierPoint;
                if(context.getCaster().getCasterEntity() instanceof ServerPlayer player){
                    player.getData(ModAttachments.PLAYER_MAGIC_DATA).setBarrierPoint(currentBarrierPoint + barrierPoint);
                    consumeMaso(BASECOST * barrierPoint, context.getCaster());
                }
            }
        }else {
            barrierPoint = currentMaso / BASECOST;
            if(maxBarrierPoint >= barrierPoint + currentBarrierPoint){
                if(context.getCaster().getCasterEntity() instanceof ServerPlayer player){
                    player.getData(ModAttachments.PLAYER_MAGIC_DATA).setBarrierPoint(currentBarrierPoint + barrierPoint);
                    consumeMaso(currentMaso, context.getCaster());
                }
            }else{
                barrierPoint = Math.min(barrierPoint, maxBarrierPoint - currentBarrierPoint);
                if(context.getCaster().getCasterEntity() instanceof ServerPlayer player){
                    player.getData(ModAttachments.PLAYER_MAGIC_DATA).setBarrierPoint(currentBarrierPoint + barrierPoint);
                    consumeMaso(BASECOST * barrierPoint, context.getCaster());
                }
            }
        }
    }
}
