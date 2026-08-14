package com.github.sweetfish111.reincarnated.magic.nodes.sensor;

import com.github.sweetfish111.reincarnated.magic.caster.IMagicCaster;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
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

            spawnCriticalLineParticles(context.getLevel(), eyePos, endPos);

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
    private void spawnCriticalLineParticles(ServerLevel level, Vec3 from, Vec3 to) {
        double distance = from.distanceTo(to);
        if (distance <= 0.0) return;

        double step = 0.35; // パーティクル間の間隔（値を小さくするとより密な線になります）
        int count = (int) (distance / step);
        Vec3 direction = to.subtract(from).normalize();

        for (int i = 1; i <= count; i++) {
            Vec3 pos = from.add(direction.scale(i * step));
            // sendParticles(Particle, x, y, z, count, xOffset, yOffset, zOffset, speed)
            // count=1, offset=0, speed=0 にすることでその場に綺麗に静止して線を描きます
            level.sendParticles(
                    ParticleTypes.CRIT,
                    pos.x - 0.1, pos.y - 0.2, pos.z - 0.1,
                    1,
                    0.0, 0.0, 0.0,
                    0.0
            );
        }
    }
}
