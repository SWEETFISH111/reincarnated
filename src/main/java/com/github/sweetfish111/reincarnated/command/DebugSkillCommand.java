package com.github.sweetfish111.reincarnated.command;

import com.mojang.brigadier.CommandDispatcher;
import com.github.sweetfish111.reincarnated.init.ModAttachments;
import com.github.sweetfish111.reincarnated.player.PlayerMagicData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = "reincarnated")
public class DebugSkillCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("reincarnated")
                        .then(Commands.literal("debug")
                                .then(Commands.literal("skill")
                                        // 引数無し：自分自身を確認（権限不要）
                                        .executes(ctx -> showSkillDebug(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                                        // 引数あり：他人を確認（OP権限が必要）
                                        .then(Commands.argument("target", EntityArgument.player())
                                                .requires(src -> {
                                                    try {
                                                        ServerPlayer player = src.getPlayerOrException();
                                                        return src.getServer().getPlayerList().isOp(new NameAndId(player.getGameProfile()));
                                                    } catch (Exception e) {
                                                        return src.getServer().isSingleplayerOwner(null); // 簡易フォールバック、要調整
                                                    }
                                                })
                                                .executes(ctx -> showSkillDebug(ctx.getSource(), EntityArgument.getPlayer(ctx, "target")))
                                        )
                                )
                        )
        );
    }

    private static int showSkillDebug(CommandSourceStack source, ServerPlayer player) {
        PlayerMagicData data = player.getData(ModAttachments.PLAYER_MAGIC_DATA);

        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(player.getName().getString()).append(" のユニークスキル状態 ===\n");
        sb.append("現在のスキル: ").append(data.getCurrentUniqueSkill()).append("\n");
        sb.append("greedy完了: ").append(data.isCompleteGreedy()).append("\n");
        sb.append(String.format("greedyScore: %.2f\n", data.getGreedyScore()));
        sb.append(String.format("predatorScore: %.2f\n", data.getPredatorScore()));
        sb.append(String.format("scavengerScore: %.2f\n", data.getScavengerScore()));
        sb.append(String.format("hoarderScore: %.2f\n", data.getHoarderScore()));
        sb.append(String.format("usurperScore: %.2f\n", data.getUsurperScore()));
        sb.append("進化候補: ").append(data.getEvolvableUniqueSkills());

        Component message = Component.literal(sb.toString());
        source.sendSuccess(() -> message, false);
        return 1;
    }
}