package com.github.sweetfish111.reincarnated.magic.caster;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public record CasterSnapshot (
    Vec3 position,
    Vec3 eyePosition,
    Vec3 lookVector
){
public static CasterSnapshot capture(IMagicCaster caster) {
    return new CasterSnapshot(
            caster.getCasterPosition(),
            caster.getEyePosition(),
            caster.getLookVector()
    );
}
        }
