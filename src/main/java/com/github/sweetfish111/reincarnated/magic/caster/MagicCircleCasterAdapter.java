package com.github.sweetfish111.reincarnated.magic.caster;

import com.github.sweetfish111.reincarnated.blockentity.MagicCircleEntity;
import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class MagicCircleCasterAdapter implements IMagicCaster{
    private final MagicCircleEntity entity;

    public MagicCircleCasterAdapter(MagicCircleEntity entity) {
        this.entity = entity;
    }

    @Override
    public void addMaso(float amount) {
        //何もしない
    }

    @Override
    public void addTotalRegeneratedMaso(float amount) {
        //何もしない
    }

    @Override
    public void consumeMaso(float amount) {
        //何もしない
    }

    @Override
    public Entity getCasterEntity() {
        return null;
    }

    @Override
    public UUID getCasterId() {
        return UUID.nameUUIDFromBytes(entity.getBlockPos().toString().getBytes());
    }

    @Override
    public ServerLevel getCasterLevel() {
        return (ServerLevel) entity.getLevel();
    }

    @Override
    public Vec3 getCasterPosition() {
        return Vec3.atBottomCenterOf(entity.getBlockPos());
    }

    @Override
    public MagiculeCircuit getCircuit() {
        return entity.getCircuit();
    }

    @Override
    public Vec3 getEyePosition() {
        return null;
    }

    @Override
    public Container getInventoryContainer() {
        return entity.getInventory();
    }

    @Override
    public Vec3 getLookVector() {
        return null;
    }

    @Override
    public float getMasoAmount() {
        return (float) entity.getMasoTank().getBalance();
    }

    @Override
    public double getMasoTankCapacity() {
        return entity.getMasoTank().getLimit();
    }

    @Override
    public boolean ownsCircuit(MagiculeCircuit circuit) {
        return circuit == entity.getCircuit();
    }
}
