package com.github.sweetfish111.reincarnated.magic.nodes;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;

import java.util.Optional;

public class GetLookTargetNode extends AbstractMagicNode{
    @Override
    public void execute(MagicContext context){
        //何もしない
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context){
       System.out.println("[sisen] zahyou wo youkyuusareta");
        if(portIndex == 0){
            Player player = context.getCaster();
            double maxDistance = 80.0D;

            Vec3 eyePos = player.getEyePosition();
            Vec3 lookVec = player.getLookAngle();
            Vec3 endPos = eyePos.add(lookVec.scale(maxDistance));

            BlockHitResult blockHit = player.level().clip(new ClipContext(eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
            double hitDistance = blockHit.getType() != HitResult.Type.MISS ? blockHit.getLocation().distanceTo(eyePos) : maxDistance;
            Vec3 rayEnd = endPos.add(lookVec.scale(hitDistance));

            AABB searchBox = player.getBoundingBox().expandTowards(lookVec.scale(maxDistance)).inflate(1.0D);
            EntityHitResult closestEntityHit = null;
            double closestDist = hitDistance;

            for(Entity entity : player.level().getEntities(player, searchBox, e -> !e.isSpectator())){
                AABB entityBox = entity.getBoundingBox().inflate(entity.getPickRadius());

                Optional<Vec3> hitOpt = entityBox.clip(eyePos, rayEnd);

                if(hitOpt.isPresent()){
                    double dist = eyePos.distanceTo((hitOpt.get()));

                    if(dist < closestDist){
                        closestDist = dist;
                        closestEntityHit = new EntityHitResult(entity, hitOpt.get());
                    }
                }
            }

            if(closestEntityHit != null && closestEntityHit.getEntity() != null){
                return closestEntityHit.getEntity().blockPosition();
            }else if(blockHit.getType() == HitResult.Type.BLOCK){
                return blockHit.getBlockPos();
            }
        }
        System.out.println("[sisen] nani mo nakatta");
        return null;
    }
}
