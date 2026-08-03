package com.github.sweetfish111.reincarnated.event.handler;

import com.github.sweetfish111.reincarnated.init.ModAttachments;
import com.github.sweetfish111.reincarnated.player.PlayerMagicData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = "reincarnated")
public class OnLivingDamageHandler {
    @SubscribeEvent
    public static void playerDamage(LivingDamageEvent.Post event){
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerMagicData magicData = player.getData(ModAttachments.PLAYER_MAGIC_DATA);

            // magicData.getActiveSkillCircuit().triggerOnDamage(player, event.getAmount(), event.getSource().getEntity());
        }
    }
}
