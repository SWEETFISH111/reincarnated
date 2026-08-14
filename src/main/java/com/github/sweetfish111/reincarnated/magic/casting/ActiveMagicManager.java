package com.github.sweetfish111.reincarnated.magic.casting;

import com.github.sweetfish111.reincarnated.circuit.EditorTab;
import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.circuit.RuntimeMagicCircuit;
import com.github.sweetfish111.reincarnated.init.ModAttachments;
import com.github.sweetfish111.reincarnated.magic.caster.IMagicCaster;
import com.github.sweetfish111.reincarnated.magic.caster.PlayerCasterAdapter;
import com.github.sweetfish111.reincarnated.magic.compiler.MagicCompiler;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.magic.nodes.MagicNode;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ActiveMagicManager {
    // プレイヤーのUUIDごとに、現在稼働しているアクティブ（常駐・パッシブ）ノードのマップ
    // ※ Listに CopyOnWriteArrayList を採用し、並行修正例外（ConcurrentModificationException）を完全にブロック！
    private static final Map<UUID, List<ActiveNodeEntry>> activeRegistry = new ConcurrentHashMap<>();

    /**
     * 常駐ノードのエントリークラス
     */
    public static class ActiveNodeEntry {
        private final UUID nodeId;
        private final MagicNode nodeInstance;
        private final MagiculeCircuit sourceCircuit;
        private final int intervalTicks; // スロットリング用（例: 5Tickに1回実行など）
        private int tickCounter = 0;

        public ActiveNodeEntry(UUID nodeId, MagicNode nodeInstance, MagiculeCircuit sourceCircuit, int intervalTicks) {
            this.nodeId = nodeId;
            this.nodeInstance = nodeInstance;
            this.sourceCircuit = sourceCircuit;
            this.intervalTicks = Math.max(1, intervalTicks);
        }

        public boolean shouldExecute() {
            tickCounter++;
            if (tickCounter >= intervalTicks) {
                tickCounter = 0;
                return true;
            }
            return false;
        }

        public void execute(IMagicCaster caster) {
            RuntimeMagicCircuit runtimeMagicCircuit = MagicCompiler.compileCircuit(caster, sourceCircuit);
            if (runtimeMagicCircuit != null) {
                MagicContext context = new MagicContext(sourceCircuit, runtimeMagicCircuit);
                AbstractMagicNode node = runtimeMagicCircuit.getInstancedNode(nodeId);
                if(node != null){
                    node.execute(context);
                }
            }
        }

        public UUID getNodeId() {
            return nodeId;
        }
    }

    /**
     * プレイヤーが新しい常駐ノードを有効化したときに登録する
     */
    public static void registerActiveNode(IMagicCaster caster, UUID nodeId, MagicNode node, MagiculeCircuit sourceCircuit,  int intervalTicks) {
        activeRegistry.computeIfAbsent(caster.getCasterId(), k -> new CopyOnWriteArrayList<>())
                .removeIf(entry -> entry.getNodeId().equals(nodeId)); // 既存の重複を防ぐ

        activeRegistry.get(caster.getCasterId()).add(new ActiveNodeEntry(nodeId, node, sourceCircuit, intervalTicks));
    }

    /**
     * 常駐ノードを無効化したとき（またはログアウト時）に解除する
     */
    public static void unregisterActiveNode(IMagicCaster caster, UUID nodeId) {
        List<ActiveNodeEntry> entries = activeRegistry.get(caster.getCasterId());
        if (entries != null) {
            entries.removeIf(entry -> entry.getNodeId().equals(nodeId));
        }
    }

    /**
     * プレイヤーごとの全アクティブノードをクリアする（死亡時やリログ時など）
     */
    public static void unregisterAllForPlayer(UUID playerUuid) {
        if (playerUuid != null) {
            activeRegistry.remove(playerUuid);
        }
    }

    /**
     * 👑 サーバーの心臓部：毎Tickのイベントから呼び出す実行ループ
     */
    public static void onServerTick(ServerLevel level) {
        // 現在ワールドにいる全プレイヤーに対してアクティブノードを安全に処理
        for (ServerPlayer player : level.players()) {
            List<ActiveNodeEntry> entries = activeRegistry.get(player.getUUID());
            if (entries == null || entries.isEmpty()) continue;

            // CopyOnWriteArrayList を使っているため安全ですが、
            // 念のため新しいリストにスナップショットを取ってイテレートすることで衝突を完全に防止します
            List<ActiveNodeEntry> safeEntries = new ArrayList<>(entries);
            for (ActiveNodeEntry entry : safeEntries) {
                // スロットリング（間引き処理）の判定を挟むことでサーバー負荷を劇的に軽減！
                if (entry.shouldExecute()) {
                    entry.execute(new PlayerCasterAdapter(player));
                }
            }
        }
    }

    public static void executeEventTrigger(IMagicCaster caster, EditorTab tab, String triggerNodeType, Map<String, Object> eventData) {
        // 指定されたタブの回路とコンパイル済みデータを取り出す
        if (caster instanceof PlayerCasterAdapter p) {
            ServerPlayer player = (ServerPlayer) p.getCasterEntity();
            MagiculeCircuit circuit = player.getData(ModAttachments.PLAYER_MAGIC_DATA).getCircuit(tab);
            if (circuit == null) return;

            RuntimeMagicCircuit runtimeCircuit = MagicCompiler.compileCircuit(caster, circuit);
            if (runtimeCircuit == null) return;

            for (Map.Entry<UUID, AbstractMagicNode> entry : runtimeCircuit.getInstancedNodes().entrySet()) {
                AbstractMagicNode node = entry.getValue();
                if (Objects.equals(node.getTriggerType(), triggerNodeType)) {
                    node.setEventData(eventData);
                    node.execute(new MagicContext(circuit, runtimeCircuit));
                }
            };
        }
    }
}