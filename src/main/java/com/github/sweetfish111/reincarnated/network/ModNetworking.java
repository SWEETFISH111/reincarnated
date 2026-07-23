package com.github.sweetfish111.reincarnated.network;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.circuit.PlayerMagicData;
import com.github.sweetfish111.reincarnated.client.screen.MagicEditorScreen;
import com.github.sweetfish111.reincarnated.magic.casting.CastingManager;
import com.github.sweetfish111.reincarnated.network.payload.*;
import com.github.sweetfish111.reincarnated.client.screen.MagicEditorScreen;
import com.github.sweetfish111.reincarnated.init.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

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
                        if(context.player() instanceof ServerPlayer player){
                            PlayerMagicData magicData = new PlayerMagicData();
                            magicData.loadFromNBT(payload.magicDataTag());
                            player.setData(ModAttachments.PLAYER_MAGIC_DATA, magicData);
                            System.out.println("サーバー: プレイヤー " + player.getName().getString() + " の魔法データを保存・同期しました");
                        }
                    });
                })
        );

        //魔法編集画面を開いたときに贈られるペイロードが届いた時のレジスタと処理
        registrar.playToServer(RequestCircuitPayload.TYPE, RequestCircuitPayload.CODEC, (payload, context) -> {
            context.enqueueWork(() -> {
                if(context.player() instanceof ServerPlayer player){
                    PlayerMagicData magicData = player.getData(ModAttachments.PLAYER_MAGIC_DATA);
                    context.reply(new SyncCircuitPayload(magicData.saveToNBT()));
                }
            });
        });

        //サーバーからSyncCircuitPayload（プレイヤーに保存された魔法データを実際のUIに反映させる時の手紙）を送るときのレジスタと処理
        registrar.playToClient(SyncCircuitPayload.TYPE, SyncCircuitPayload.CODEC, ((payload, context) -> {
            context.enqueueWork(() -> {
                PlayerMagicData magicData = new PlayerMagicData();
                magicData.loadFromNBT(payload.magicDataTag());

                Minecraft.getInstance().setScreenAndShow(new MagicEditorScreen(magicData));
            });
        }));

        //魔法１キーが押されて送信されるペイロードのレジスタと処理
        registrar.playToServer(CastMagicOnePayload.TYPE, CastMagicOnePayload.CODEC,((payload, context) -> {
            context.enqueueWork(() -> {
                if(context.player() instanceof ServerPlayer player){
                    PlayerMagicData magicData = player.getData(ModAttachments.PLAYER_MAGIC_DATA);
                    MagiculeCircuit circuit = magicData.getCircuit(MagicEditorScreen.EditorTab.MAGIC);
                    if(circuit != null){
                        System.out.println(player.getName().getString() + "is press magic_key_1. compiling magic circuit");
                        System.out.println("loaded nodes length" + circuit.getNodes().size() + "/wire length" + circuit.getWires().size());
                        CastingManager.startCasting(player, circuit, "event_key_1");
                    }else{
                        System.out.println("circuit not found");
                    }
                }
            });
        }));

        registrar.playToServer(StopCastPayload.TYPE, StopCastPayload.CODEC, ((payload, context) -> {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    // キーが離されたので、詠唱完了状態なら魔法を発動、途中ならキャンセルの判定を依頼
                    CastingManager.releaseCasting(player, payload.triggerType());
                }
            });
        }));
    }
}
