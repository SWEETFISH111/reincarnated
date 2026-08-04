package com.github.sweetfish111.reincarnated.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

public class PlayerUniqueSkillAcquiredEvent extends Event {
    private final ServerPlayer player;
    private final String skillName;

    public PlayerUniqueSkillAcquiredEvent(ServerPlayer player, String skillName) {
        this.player = player;
        this.skillName = skillName;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public String getSkillName() {
        return skillName;
    }
}