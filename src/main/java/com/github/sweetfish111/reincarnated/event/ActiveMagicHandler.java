package com.github.sweetfish111.reincarnated.event;

import com.github.sweetfish111.reincarnated.magic.ActiveMagicManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public class ActiveMagicHandler {
    public static void onServerTick(MinecraftServer server){
        for (ServerLevel level : server.getAllLevels()) {
            ActiveMagicManager.onServerTick(level);
        }
    }
}
