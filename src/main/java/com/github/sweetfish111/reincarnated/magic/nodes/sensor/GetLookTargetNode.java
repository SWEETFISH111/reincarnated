package com.github.sweetfish111.reincarnated.magic.nodes.sensor;

import com.github.sweetfish111.reincarnated.magic.caster.IMagicCaster;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;

import java.util.Optional;
import java.util.UUID;

public class GetLookTargetNode extends AbstractMagicNode {
    public GetLookTargetNode(UUID id) {
        super(id);
    }

    @Override
    public void execute(MagicContext context){
        //何もしない
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context){
        super.getOutputData(portIndex, context);
        if(portIndex == 0){
            Entity entity = context.getCaster().getCasterEntity();
            double maxDistance = 80.0D;

            Vec3 eyePos = entity.getEyePosition();
            Vec3 lookVec = entity.getLookAngle();
            Vec3 endPos = eyePos.add(lookVec.scale(maxDistance));

            BlockHitResult blockHit = entity.level().clip(new ClipContext(eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity));
            double hitDistance = blockHit.getType() != HitResult.Type.MISS ? blockHit.getLocation().distanceTo(eyePos) : maxDistance;
            Vec3 rayEnd = endPos.add(lookVec.scale(hitDistance));

            AABB searchBox = entity.getBoundingBox().expandTowards(lookVec.scale(maxDistance)).inflate(1.0D);
            EntityHitResult closestEntityHit = null;
            double closestDist = hitDistance;

            for(Entity target : entity.level().getEntities(entity, searchBox, e -> !e.isSpectator())){
                AABB entityBox = target.getBoundingBox().inflate(entity.getPickRadius());

                Optional<Vec3> hitOpt = entityBox.clip(eyePos, rayEnd);

                if(hitOpt.isPresent()){
                    double dist = eyePos.distanceTo((hitOpt.get()));

                    if(dist < closestDist){
                        closestDist = dist;
                        closestEntityHit = new EntityHitResult(target, hitOpt.get());
                    }
                }
            }

            if(closestEntityHit != null){
                return closestEntityHit.getEntity();
            }
        }

        return null;
    }
}
