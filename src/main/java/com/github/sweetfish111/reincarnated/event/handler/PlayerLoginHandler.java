package com.github.sweetfish111.reincarnated.event.handler;

import com.github.sweetfish111.reincarnated.circuit.EditorTab;
import com.github.sweetfish111.reincarnated.event.PlayerUniqueSkillAcquiredEvent;
import com.github.sweetfish111.reincarnated.init.ModAttachments;
import com.github.sweetfish111.reincarnated.magic.casting.ActiveMagicManager;
import com.github.sweetfish111.reincarnated.magic.casting.PassiveSlotManager;
import com.github.sweetfish111.reincarnated.player.PlayerMagicData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = "reincarnated")
class PlayerLoginHandler {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PlayerMagicData magicData = serverPlayer.getData(ModAttachments.PLAYER_MAGIC_DATA);
            magicData.addDefaultUnlockedNodes(EditorTab.MAGIC);
            magicData.addDefaultUnlockedNodes(EditorTab.SKILL);
            magicData.addDefaultUnlockedNodes(EditorTab.ARTS);

            if ("greedy".equals(magicData.getCurrentUniqueSkill()) && !magicData.hasUnlocked("greedy_welcomed")) {
                magicData.unlock("greedy_welcomed");

                NeoForge.EVENT_BUS.post(new PlayerUniqueSkillAcquiredEvent(serverPlayer, "greedy"));
            }

            for (int i = 0; i < PlayerMagicData.MAGIC_SLOT_COUNT; i++) {
                if (magicData.isMagicSlotEnabled(i)) {
                    PassiveSlotManager.startSlot(serverPlayer, magicData.getMagicSlot(i));
                }
            }

            ActiveMagicManager.scanAndRegisterResidentNodes(serverPlayer);
        }
    }

}