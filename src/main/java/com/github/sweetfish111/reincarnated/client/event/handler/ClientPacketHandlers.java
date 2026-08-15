package com.github.sweetfish111.reincarnated.client.event.handler;

import com.github.sweetfish111.reincarnated.client.MagiculeHUDOverlay;
import com.github.sweetfish111.reincarnated.client.screen.MagicEditorScreen;
import com.github.sweetfish111.reincarnated.client.screen.StatusScreen;
import com.github.sweetfish111.reincarnated.network.payload.SyncStatusPayload;
import com.github.sweetfish111.reincarnated.player.PlayerMagicData;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;


public class ClientPacketHandlers {
    public static void handleSyncCircuit(CompoundTag magicDataTag){
        PlayerMagicData magicData = new PlayerMagicData();
        magicData.loadFromNBT(magicDataTag);

        Minecraft.getInstance().setScreenAndShow(new MagicEditorScreen(magicData));
    }

    public static void handleSyncMaso(float maxMaso, float currentMaso, float maxBarrier, float currentBarrier){
        MagiculeHUDOverlay.maxMaso = maxMaso;
        MagiculeHUDOverlay.currentMaso = currentMaso;
        MagiculeHUDOverlay.maxBarrier = maxBarrier;
        MagiculeHUDOverlay.currentBarrire = currentBarrier;
    }

    public static void handleSyncStatus(SyncStatusPayload payload){
        Minecraft.getInstance().setScreenAndShow(new StatusScreen(payload));
    }
}
