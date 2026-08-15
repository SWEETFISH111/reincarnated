package com.github.sweetfish111.reincarnated.system;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class VoiceOfWorld {

    public static Component sendEvolvedStage1(ServerPlayer player) {
        // 1. 文字化け部分を作成（例：「§kxxxxx」のようなガチガチ動く不気味なテキスト）
        Component glitchText = Component.literal("░▒▓█░▒▓█")
                .withStyle(ChatFormatting.OBFUSCATED, ChatFormatting.DARK_PURPLE);

        // 2. プレイヤー名部分を作成（水色＆太字）
        Component playerName = player.getDisplayName().copy()
                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD);

        // 3. 翻訳キーに対して引数を流し込む
        Component message = Component.translatable(
                "message.reincarnated.voice_of_world.glitch_evolution_stage_1",
                glitchText,
                playerName
        ).withStyle(ChatFormatting.DARK_AQUA); // 全体のアナウンスカラー（世界の声）

        // 4. チャットに送信
        return  message;
    }

    public static Component sendEvolvedStage2(Player player) {

        // プレイヤー名のコンポーネント（白色で表示）
        Component playerName = Component.literal(player.getScoreboardName())
                .withStyle(ChatFormatting.WHITE);

        // 《告》アナウンスメッセージ（世界の声風にアクア色で装飾）
        Component message = Component.translatable(
                "message.reincarnated.voice_of_world.evolution_hermit",
                playerName
        ).withStyle(ChatFormatting.AQUA);

        return message;
    }
}
