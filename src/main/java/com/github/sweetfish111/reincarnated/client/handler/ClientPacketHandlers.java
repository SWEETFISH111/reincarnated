package com.github.sweetfish111.reincarnated.client.handler;

import com.github.sweetfish111.reincarnated.client.MagiculeHUDOverlay;
import com.github.sweetfish111.reincarnated.client.screen.MagicEditorScreen;
import com.github.sweetfish111.reincarnated.player.PlayerMagicData;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.common.Mod;


public class ClientPacketHandlers {
    public static void handleSyncCircuit(CompoundTag magicDataTag){
        PlayerMagicData magicData = new PlayerMagicData();
        magicData.loadFromNBT(magicDataTag);

        Minecraft.getInstance().setScreenAndShow(new MagicEditorScreen(magicData));
    }

    public static void handleSyncMaso(float maxMaso, float currentMaso){
        MagiculeHUDOverlay.max = maxMaso;
        MagiculeHUDOverlay.current = currentMaso;
    }
}
