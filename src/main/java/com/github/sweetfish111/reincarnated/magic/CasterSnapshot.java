package com.github.sweetfish111.reincarnated.magic;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public record CasterSnapshot (
    UUID playerId,
    Vec3 position,
    Vec3 lookVector,
    float health
){
public static CasterSnapshot capture(ServerPlayer player) {
    return new CasterSnapshot(
            player.getUUID(),
            player.position(),
            player.getLookAngle(),
            player.getHealth()
    );
}
        }
