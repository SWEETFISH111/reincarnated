package com.github.sweetfish111.reincarnated.event.handler;

import com.github.sweetfish111.reincarnated.event.PlayerUniqueSkillAcquiredEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "reincarnated")
public class UniqueSkillEventHandlers {

    @SubscribeEvent
    public static void onUniqueSkillAcquired(PlayerUniqueSkillAcquiredEvent event) {
        ServerPlayer player = event.getPlayer();
        String skillName = event.getSkillName();

        if ("greedy".equals(skillName)) {
            String playerName = player.getName().getString();

            Component formattedPlayerName = Component.literal(playerName).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD);
            Component obfuscatedFactror = Component.literal("¶§!?#@%").withStyle(ChatFormatting.OBFUSCATED, ChatFormatting.RED);

            Component message = Component.translatable("message.reincarnated.unique_skill_acquired",
                    formattedPlayerName,
                    obfuscatedFactror).withStyle(ChatFormatting.DARK_PURPLE);
            player.sendSystemMessage(message);
        }
    }
}