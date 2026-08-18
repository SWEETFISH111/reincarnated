package com.github.sweetfish111.reincarnated.magic.nodes.sensor;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

/**
 * 指定座標を中心とした球状範囲内で最も近いLivingEntityを1体返すセンサーノード。
 * 入力: 中心座標(Vec3), 半径(Number), 自分自身を除外するか(Boolean、任意)
 * 出力: 最も近いLivingEntity（見つからなければnull）
 */
public class GetNearestEntityInRadius extends AbstractMagicNode {
    public GetNearestEntityInRadius(UUID id) {
        super(id);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(portIndex, context);

        Vec3 center = pullVector3(0, context);
        double radius = pullDouble(1, context);
        if (center == null || radius <= 0) return null;

        ServerLevel level = context.getLevel();
        if (level == null) return null;

        AABB searchBox = new AABB(
                center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius
        );

        List<LivingEntity> candidates = level.getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                e -> e.isAlive() && e.position().distanceToSqr(center) <= radius * radius
        );

        LivingEntity nearest = null;
        double nearestDistSq = Double.MAX_VALUE;
        for (LivingEntity entity : candidates) {
            double distSq = entity.position().distanceToSqr(center);
            if (distSq < nearestDistSq && !entity.equals(context.getCaster().getCasterEntity())) {
                nearestDistSq = distSq;
                nearest = entity;
            }
        }
        return nearest;
    }
}