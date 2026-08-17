package com.github.sweetfish111.reincarnated.command;

import com.mojang.brigadier.CommandDispatcher;
import com.github.sweetfish111.reincarnated.world.LandMasoDensityData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = "reincarnated")
public class DebugWorldCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("reincarnated")
                        .then(Commands.literal("debug")
                                .then(Commands.literal("maso_density")
                                        .executes(ctx -> {
                                            CommandSourceStack source = ctx.getSource();
                                            ServerPlayer player = source.getPlayerOrException();
                                            if (source.getLevel() instanceof ServerLevel level) {
                                                float density = LandMasoDensityData.get(level).getDensity(level, player.blockPosition());
                                                source.sendSuccess(() -> Component.literal(
                                                        String.format("この地点の魔素濃度: %.2f", density)), false);
                                            }
                                            return 1;
                                        })
                                )
                        )
        );
    }
}