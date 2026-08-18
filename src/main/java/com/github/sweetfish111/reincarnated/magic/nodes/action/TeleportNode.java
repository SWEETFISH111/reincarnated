package com.github.sweetfish111.reincarnated.magic.nodes.action;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.system.ReincarnatedPlaySound;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * 入力座標へ術者を転移させる汎用ノード。
 * 座標の出どころは問わない(固定VECTOR、GET_LOOK_TARGET、ChannelReceive等どれでも接続可能)。
 */
public class TeleportNode extends AbstractMagicNode {
    double BASECOST = 8;

    public TeleportNode(UUID id) {
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
        context.incrementAndCheck();
        Vec3 pos = pullVector3(2, context);
        Object rawTarget = pullData(1, context);
        if(rawTarget instanceof LivingEntity entity){
            if (pos != null && context.getCaster().getCasterEntity() instanceof ServerPlayer player) {
                entity.teleportTo(pos.x, pos.y, pos.z);
                ReincarnatedPlaySound.playTeleportSound(context.getCaster().getCasterLevel(), pos);
            }
        }
        pushExecute(context);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(portIndex, context);
        return BASECOST;
    }
}