package com.github.sweetfish111.reincarnated.event.handler;

import com.github.sweetfish111.reincarnated.event.PlayerUniqueSkillAcquiredEvent;
import com.github.sweetfish111.reincarnated.init.ModAttachments;
import com.github.sweetfish111.reincarnated.player.PlayerMagicData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = "reincarnated")
public class UniqueSkillEventHandlers {

    @SubscribeEvent
    public static void onUniqueSkillAcquired(PlayerUniqueSkillAcquiredEvent event) {
        ServerPlayer player = event.getPlayer();
        String skillName = event.getSkillName();

        if ("greedy".equals(skillName)) {
            String playerName = player.getName().getString();

            Component message = Component.empty()
                    .append(Component.literal("《告》個体名 ").withStyle(ChatFormatting.DARK_PURPLE))
                    .append(Component.literal(playerName).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                    .append(Component.literal(" に ").withStyle(ChatFormatting.DARK_PURPLE))
                    .append(Component.literal("¶§!?#@%").withStyle(ChatFormatting.OBFUSCATED, ChatFormatting.RED))
                    .append(Component.literal(" の因子を確認。ユニークスキル『貪欲者』を獲得……成功しました。").withStyle(ChatFormatting.DARK_PURPLE));

            player.sendSystemMessage(message);
        }
    }
}