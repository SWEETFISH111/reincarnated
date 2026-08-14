package com.github.sweetfish111.reincarnated.magic.nodes.sensor;

import com.github.sweetfish111.reincarnated.magic.caster.CasterSnapshot;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class GetLookForwardNode extends AbstractMagicNode {

    public GetLookForwardNode(UUID nodeId) {
        super(nodeId);
    }

    @Override
    public void execute(MagicContext context) {
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(portIndex, context);
        Object distanceData = pullData(1, context);
        Object rawTarget = pullData(0, context);
        double maxDistance = distanceData instanceof Number num ? num.doubleValue() : 16.0;

        Entity caster = context.getCaster().getCasterEntity();
        Vec3 eyePos = caster.getEyePosition();
        Vec3 lookVec = caster.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.scale(maxDistance));

        if (rawTarget instanceof LivingEntity entity) {
            eyePos = entity.getEyePosition();
            lookVec = entity.getLookAngle();
            endPos = eyePos.add(lookVec.scale(maxDistance));
        } else if (rawTarget instanceof CasterSnapshot snapshot) {
            eyePos = snapshot.eyePosition();
            lookVec = snapshot.lookVector();
            endPos = eyePos.add(lookVec.scale(maxDistance));
        }

        // ブロックとの衝突判定（クリッピング）
        BlockHitResult blockHit = context.getLevel().clip(
                new ClipContext(eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, caster)
        );

        boolean isBlockHit = (blockHit.getType() == HitResult.Type.BLOCK);
        // ブロックに当たった場合は衝突地点まで、当たらなかった場合は最大射程まで線を引く
        Vec3 actualHitPos = isBlockHit ? blockHit.getLocation() : endPos;

        // --- 🎯 始点からターゲット衝突地点までクリティカルパーティクルで線を引く ---
        spawnCriticalLineParticles(context.getLevel(), eyePos, actualHitPos);

        boolean snapToBlock = false;
        if (context.getCircuit().getNodeParam(this.id, "value", false) instanceof Boolean b) {
            snapToBlock = b;
        }

        if (portIndex == 0) {
            if (snapToBlock && isBlockHit) {
                return blockHit.getBlockPos();
            }
            return isBlockHit ? actualHitPos : endPos;
        }

        if (portIndex == 1) {
            if (snapToBlock && isBlockHit) {
                Direction face = blockHit.getDirection();
                Vec3 surfaceNormal = new Vec3(
                        face.getStepX(),
                        face.getStepY(),
                        face.getStepZ()
                );
                return surfaceNormal.scale(-1.0);
            }
        }

        return null;
    }

    /**
     * 始点 (from) から 終点 (to) へ向かって等間隔にクリティカルパーティクルを生成する
     */
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
                    pos.x - 0.1, pos.y - 0.2, pos.z -0.1,
                    1,
                    0.0, 0.0, 0.0,
                    0.0
            );
        }
    }
}