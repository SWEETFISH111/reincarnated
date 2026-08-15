package com.github.sweetfish111.reincarnated.magic.summon;

import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class SummonedInstance {
    private final UUID summonId;
    private final UUID ownerId;
    private Vec3 position;
    private float yRot;
    private float xRot;
    private int remainingTicks;
    private final SummonBehavior behavior;

    public SummonedInstance(UUID summonId, UUID ownerId, Vec3 position, int livingTicks, SummonBehavior behavior) {
        this.summonId = summonId;
        this.ownerId = ownerId;
        this.position = position;
        this.remainingTicks = livingTicks;
        this.behavior = behavior;
    }

    public UUID getSummonId() { return summonId; }
    public UUID getOwnerId() { return ownerId; }
    public Vec3 getPosition() { return position; }
    public void setPosition(Vec3 position) { this.position = position; }
    public float getYRot() { return yRot; }
    public void setYRot(float yRot) { this.yRot = yRot; }
    public float getXRot() { return xRot; }
    public void setXRot(float xRot) { this.xRot = xRot; }
    public SummonBehavior getBehavior() { return behavior; }

    /** @return trueなら寿命切れ */
    public boolean tickLifespan() {
        remainingTicks--;
        return remainingTicks <= 0;
    }
}