package com.github.sweetfish111.reincarnated.magic.caster;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public interface IMagicCaster {
        Entity getCasterEntity();
        UUID getCasterId();
        MagiculeCircuit getCircuit();
        Vec3 getCasterPosition();
        ServerLevel getCasterLevel();
        Vec3 getLookVector();
        float getMasoAmount();
        void addMaso(float amount);
        void consumeMaso(float amount);
        void addTotalRegeneratedMaso(float amount);
}
