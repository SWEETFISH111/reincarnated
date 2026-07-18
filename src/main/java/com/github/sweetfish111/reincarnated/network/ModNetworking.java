package com.github.sweetfish111.reincarnated.network;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.magic.compiler.MagicCompiler;
import net.minecraft.client.KeyMapping;
import com.github.sweetfish111.reincarnated.client.screen.MagicEditorScreen;
import com.github.sweetfish111.reincarnated.init.ModAttachments;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = "reincarnated")
public class ModNetworking {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event){
        final PayloadRegistrar registrar = event.registrar("reincarnated");
        //魔法編集を終えてUIを閉じたときのペイロードのレジスタと処理
        registrar.playToServer(
          SaveCircuitPayload.TYPE,
          SaveCircuitPayload.CODEC,
                ((payload, context) -> {
                    context.enqueueWork(() -> {
                        var player = context.player();
                        var circuitData = payload.circuitData();

                        player.setData(ModAttachments.MAGIC_CIRCUIT, circuitData);
                        System.out.println("サーバー:プレイヤー" + player.getName().getString() + "から魔法人データを受信した");
                        System.out.println("中身" + circuitData.toString());
                    });
                })
        );

        //魔法編集画面を開いたときに贈られるペイロードが届いた時のレジスタと処理
        registrar.playToServer(RequestCircuitPayload.TYPE, RequestCircuitPayload.CODEC, (payload, context) -> {
            context.enqueueWork(() -> {
                var player = context.player();
                var circuitData = player.getData(ModAttachments.MAGIC_CIRCUIT);

                System.out.println("second:from sever:" + circuitData.toString());

                context.reply(new SyncCircuitPayload(circuitData));
            });
        });

        //サーバーからSyncCircuitPayload（プレイヤーに保存された魔法データを実際のUIに反映させる時の手紙）を送るときのレジスタと処理
        registrar.playToClient(SyncCircuitPayload.TYPE, SyncCircuitPayload.CODEC, ((payload, context) -> {
            context.enqueueWork(() -> {
                Minecraft.getInstance().setScreenAndShow(
                        new MagicEditorScreen(payload.circuitData())
                );
            });
        }));

        //魔法１キーが押されて送信されるペイロードのレジスタと処理
        registrar.playToServer(CastMagicOnePayload.TYPE, CastMagicOnePayload.CODEC,((payload, context) -> {
            context.enqueueWork(() -> {
                if(context.player() instanceof ServerPlayer player){
                    CompoundTag playerTag = player.getData(ModAttachments.MAGIC_CIRCUIT);
                    MagiculeCircuit circuit = new MagiculeCircuit();
                    circuit.loadFromNBT(playerTag);
                    if(circuit != null){
                        System.out.println(player.getName().getString() + "is press magic_key_1. compiling magic circuit");
                        System.out.println("loaded nodes length" + circuit.getNodes().size() + "/wire length" + circuit.getWires().size());
                        MagicCompiler.compileAndExecute(circuit, player, "event_key_1");
                    }else{
                        System.out.println("circuit not found");
                    }
                }
            });
        }));
    }
}
