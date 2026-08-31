package com.github.sweetfish111.reincarnated.network;

import com.github.sweetfish111.reincarnated.circuit.EditorTab;
import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.circuit.RuntimeMagicCircuit;
import com.github.sweetfish111.reincarnated.client.event.handler.ClientPacketHandlers;
import com.github.sweetfish111.reincarnated.item.ReincarnatedItems;
import com.github.sweetfish111.reincarnated.magic.caster.PlayerCasterAdapter;
import com.github.sweetfish111.reincarnated.magic.casting.*;
import com.github.sweetfish111.reincarnated.magic.compiler.MagicCompiler;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.player.PlayerMagicData;
import com.github.sweetfish111.reincarnated.network.payload.*;
import com.github.sweetfish111.reincarnated.init.ReincarnatedAttachments;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.UUID;

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
                            UUID casterId = player.getUUID();

                            TimerCastingManager.cancelTasksForCaster(casterId);
                            DelayCastingManager.cancelTasksForCaster(casterId);
                            ActiveMagicManager.unregisterAllForPlayer(casterId);

                            PlayerMagicData magicData = new PlayerMagicData();
                            magicData.loadFromNBT(payload.magicDataTag());
                            player.setData(ReincarnatedAttachments.PLAYER_MAGIC_DATA, magicData);

                            for (int i = 0; i < PlayerMagicData.MAGIC_SLOT_COUNT; i++) {
                                if (magicData.isMagicSlotEnabled(i)) {
                                    PassiveSlotManager.startSlot(player, magicData.getMagicSlot(i));
                                }
                            }

                            ActiveMagicManager.scanAndRegisterResidentNodes(player);

                            System.out.println("サーバー: プレイヤー " + player.getName().getString() + " の魔法データを保存・同期しました");
                        }
                    });
                })
        );

        //魔法編集画面を開いたときに贈られるペイロードが届いた時のレジスタと処理
        registrar.playToServer(RequestCircuitPayload.TYPE, RequestCircuitPayload.CODEC, (payload, context) -> {
            context.enqueueWork(() -> {
                if(context.player() instanceof ServerPlayer player){
                    PlayerMagicData magicData = player.getData(ReincarnatedAttachments.PLAYER_MAGIC_DATA);
                    context.reply(new SyncCircuitPayload(magicData.saveToNBT()));
                }
            });
        });


        //サーバーからSyncCircuitPayload（プレイヤーに保存された魔法データを実際のUIに反映させる時の手紙）を送るときのレジスタと処理
        registrar.playToClient(SyncCircuitPayload.TYPE, SyncCircuitPayload.CODEC, ((payload, context) -> {
            context.enqueueWork(() -> {
                if(net.neoforged.fml.loading.FMLEnvironment.getDist().isClient()) {
                    com.github.sweetfish111.reincarnated.client.event.handler.ClientPacketHandlers.handleSyncCircuit(payload.magicDataTag());
                }
            });
        }));



        //ステータス画面
        registrar.playToServer(RequestStatusPayload.TYPE, RequestStatusPayload.CODEC, (payload, context) -> {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    PlayerMagicData magicData = player.getData(ReincarnatedAttachments.PLAYER_MAGIC_DATA);

                    SyncStatusPayload status = new SyncStatusPayload(
                            magicData.getMasoStage().name(),
                            magicData.getCurrentMaso(),
                            magicData.getMaxMaso(),
                            magicData.getMasoRegenRate(),
                            magicData.getMasoStylePreference(),
                            magicData.getBarrierPoint(),
                            magicData.getMaxBarrierPoint(),
                            magicData.getBarrierDamageReduction(),
                            magicData.getBarrierStylePreference(),
                            ActiveMagicManager.getComputeUsage(player.getUUID()),
                            magicData.getMaxComputeCapacity(),
                            magicData.getCurrentUniqueSkill(),
                            magicData.isCompleteGreedy(),
                            magicData.getGreedyScore(),
                            magicData.getPredatorScore(),
                            magicData.getScavengerScore(),
                            magicData.getHoarderScore(),
                            magicData.getUsurperScore(),
                            new ArrayList<>(magicData.getEvolvableUniqueSkills())
                    );

                    context.reply(status);
                }
            });
        });

        registrar.playToClient(SyncStatusPayload.TYPE, SyncStatusPayload.STREAM_CODEC, ((payload, context) -> {
            context.enqueueWork(() -> {
                if (net.neoforged.fml.loading.FMLEnvironment.getDist().isClient()) {
                    com.github.sweetfish111.reincarnated.client.event.handler.ClientPacketHandlers.handleSyncStatus(payload);
                }
            });
        }));

        //魔法１キーが押されて送信されるペイロードのレジスタと処理
        registrar.playToServer(CastMagicOnePayload.TYPE, CastMagicOnePayload.CODEC,((payload, context) -> {
            context.enqueueWork(() -> {
                if(context.player() instanceof ServerPlayer player){
                    PlayerMagicData magicData = player.getData(ReincarnatedAttachments.PLAYER_MAGIC_DATA);
                    MagiculeCircuit circuit = magicData.getCircuit(EditorTab.MAGIC);
                    if(circuit != null){
                        System.out.println(player.getName().getString() + "is press magic_key_1. compiling magic circuit");
                        System.out.println("loaded nodes length" + circuit.getNodes().size() + "/wire length" + circuit.getWires().size());
                        RuntimeMagicCircuit runtimeMagicCircuit = MagicCompiler.compileCircuit(new PlayerCasterAdapter(player), circuit);
                        if(runtimeMagicCircuit != null){
                            CastingManager.startCasting(new MagicContext(circuit, runtimeMagicCircuit));
                        }
                    }else{
                        System.out.println("circuit not found");
                    }
                }
            });
        }));

        //魔法キー１が離された瞬間に送信されるペイロードとレジスタと処理
        registrar.playToServer(StopCastPayload.TYPE, StopCastPayload.CODEC, ((payload, context) -> {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    // キーが離されたので、詠唱完了状態なら魔法を発動、途中ならキャンセルの判定を依頼
                    CastingManager.releaseCasting(new PlayerCasterAdapter(player));
                }
            });
        }));

        //魔素をサーバーとクライアントで同期する。
        registrar.playToClient(SyncMasoPayload.TYPE, SyncMasoPayload.STREAM_CODEC,((payload, context) ->{
            context.enqueueWork(() -> {
                if(FMLEnvironment.getDist().isClient()){
                    ClientPacketHandlers.handleSyncMaso(payload.maxMaso(), payload.currentMaso(), payload.maxBarrier(), payload.currentBarrier());
                }
            });
        }));

        registrar.playToServer(EvolveSkillPayload.TYPE, EvolveSkillPayload.CODEC, (payload, context) -> {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    PlayerMagicData magicData = player.getData(ReincarnatedAttachments.PLAYER_MAGIC_DATA);

                    boolean success = magicData.performEvolution(payload.skillId(), player);
                    if (success) {
                        // 常駐ノード(ON_TICK等)がSKILL回路の変化を反映するよう再スキャン
                        ActiveMagicManager.scanAndRegisterResidentNodes(player);
                        // クライアントに最新の回路・状態を送り返して画面を更新させる
                        context.reply(new SyncCircuitPayload(magicData.saveToNBT()));
                    }
                }
            });
        });

        //魔法を本にエクスポートする。
        registrar.playToServer(ExportSpellPalyload.TYPE, ExportSpellPalyload.STREAM_CODEC,(((payload, context) -> {
            context.enqueueWork(() -> {
                ServerPlayer player = (ServerPlayer) context.player();
                ItemStack mainHandItem = player.getMainHandItem();

                // プレイヤーが「白紙の本 (Items.BOOK)」を手に持っているか確認
                if (mainHandItem.is(Items.BOOK)) {
                    ItemStack grimoireStack = new ItemStack(ReincarnatedItems.GRIMOIRE.get());
                    // 1. 本のNBT(CustomData)に回路データを書き込む
                    grimoireStack.set(DataComponents.CUSTOM_DATA, CustomData.of(payload.circuitTag()));

                    // 2. フレーバーとして本の名前に魔法っぽさを付与（任意）
                    grimoireStack.set(DataComponents.CUSTOM_NAME, Component.translatable("item.reincarnated.grimoire").setStyle(Style.EMPTY.withItalic(false)));

                    mainHandItem.shrink(1);
                    if (!player.getInventory().add(grimoireStack)) {
                        // インベントリがいっぱいの場合は足元にドロップするなどの安全策
                        player.drop(grimoireStack, false);
                    }
                    // 3. 成功メッセージなどを送る（お好みで）
                    player.sendSystemMessage(Component.translatable("message.reincarnated.export_success"));
                } else {
                    player.sendSystemMessage(Component.translatable("message.reincarnated.export_error"));
                }
            });
        })));

        registrar.playToServer(SelectMagicSlotPayload.TYPE, SelectMagicSlotPayload.CODEC, ((payload, context) -> {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    PlayerMagicData magicData = player.getData(ReincarnatedAttachments.PLAYER_MAGIC_DATA);
                    magicData.setActiveMagicSlot(payload.slotIndex());
                }
            });
        }));

        registrar.playToServer(ToggleMagicSlotPayload.TYPE, ToggleMagicSlotPayload.CODEC, ((payload, context) -> {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    PlayerMagicData magicData = player.getData(ReincarnatedAttachments.PLAYER_MAGIC_DATA);
                    boolean wasEnabled = magicData.isMagicSlotEnabled(payload.slotIndex());
                    if (wasEnabled == payload.enabled()) return; // 変化なし

                    magicData.setMagicSlotEnabled(payload.slotIndex(), payload.enabled());
                    MagiculeCircuit slotCircuit = magicData.getMagicSlot(payload.slotIndex());

                    if (payload.enabled()) {
                        PassiveSlotManager.startSlot(player, slotCircuit);
                    } else {
                        PassiveSlotManager.stopSlot(player, slotCircuit);
                    }
                }
            });
        }));
    }
}
