package com.github.sweetfish111.reincarnated.magic.nodes;

import com.github.sweetfish111.reincarnated.entity.reincarnatedEntityTypes;
import com.github.sweetfish111.reincarnated.magic.CustomMagicProjectileEntity;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import net.minecraft.world.entity.Entity;

public class ShootProjectileNode extends AbstractMagicNode{
    @Override
    public void execute(MagicContext context) {
        Vec3 spawnPos = pullVector3(1, context);
        Vec3 direction = pullVector3(2, context);
        double size = pullDouble(3, context);
        double speed = pullDouble(4, context);

        if (spawnPos == null || direction == null) {
            System.out.println("❌ ShootProjectileNode: zahyou ka muki ga null");
            return;
        }

        System.out.println("shoot: spawnpos=" + spawnPos);

        ServerLevel level = context.getLevel();

        if (level == null) {
            System.out.println("❌ ShootProjectileNode: ServerLevel ga null");
            return;
        }

        CustomMagicProjectileEntity projectile = new CustomMagicProjectileEntity(
                reincarnatedEntityTypes.CUSTOM_MAGIC_PROJECTILE.get(),
                level
        );

        if (size > 0) {
            projectile.setSize((float) size);
        }

        projectile.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        projectile.shoot(direction.x, direction.y, direction.z, (float) speed, 1.0f);

        MagicNode nextNode = getNextNode(0);
        if (nextNode != null) {
            projectile.setMagicExecutionData(nextNode, context);
        }

        boolean success = level.addFreshEntity(projectile);
        System.out.println("toushabutu no supawn kekka: " + success + " (pos: " + spawnPos + ")");
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        Object val = context.getMagicValue("hit_pos");
        if(portIndex == 1 && val instanceof Vec3 targetPos) {
            return targetPos;
        }

        Object val2 = context.getMagicValue("hit_entity");
        if(portIndex == 2 && val2 instanceof Entity target){
            return target;
        }

        return null;
    }
}
