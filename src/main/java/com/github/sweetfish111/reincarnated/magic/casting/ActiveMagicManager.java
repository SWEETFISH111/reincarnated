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
import com.github.sweetfish111.reincarnated.player.PlayerMagicData;
import com.github.sweetfish111.reincarnated.config.BalanceConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ActiveMagicManager {
    private static final Map<UUID, List<ActiveNodeEntry>> activeRegistry = new ConcurrentHashMap<>();

    /**
     * 常駐ノードのエントリークラス。
     * 登録時点でコンパイル済みの RuntimeMagicCircuit / MagiculeCircuit を保持し、
     * 毎tickの実行では「そのノード単体」だけを直接execute()する
     * （以前は回路全体を毎回再コンパイル&start()していたため、
     *   同じ回路内の他のトリガーノードまで誤発火する不具合があった）。
     */
    public static class ActiveNodeEntry {
        private final UUID nodeId;
        private final AbstractMagicNode nodeInstance;
        private final MagiculeCircuit sourceCircuit;
        private final RuntimeMagicCircuit runtimeCircuit;
        private final int intervalTicks;
        private int tickCounter = 0;

        public ActiveNodeEntry(UUID nodeId, AbstractMagicNode nodeInstance, MagiculeCircuit sourceCircuit, RuntimeMagicCircuit runtimeCircuit, int intervalTicks) {
            this.nodeId = nodeId;
            this.nodeInstance = nodeInstance;
            this.sourceCircuit = sourceCircuit;
            this.runtimeCircuit = runtimeCircuit;
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

        public void execute() {
            MagicContext context = new MagicContext(sourceCircuit, runtimeCircuit);
            nodeInstance.execute(context);
        }

        public UUID getNodeId() { return nodeId; }
    }

    /**
     * プレイヤーが新しい常駐ノードを有効化したときに登録する
     */
    public static void registerActiveNode(IMagicCaster caster, UUID nodeId, AbstractMagicNode node, MagiculeCircuit sourceCircuit, RuntimeMagicCircuit runtimeCircuit, int intervalTicks) {
        activeRegistry.computeIfAbsent(caster.getCasterId(), k -> new CopyOnWriteArrayList<>())
                .removeIf(entry -> entry.getNodeId().equals(nodeId));

        activeRegistry.get(caster.getCasterId()).add(new ActiveNodeEntry(nodeId, node, sourceCircuit, runtimeCircuit, intervalTicks));
    }

    public static void unregisterActiveNode(IMagicCaster caster, UUID nodeId) {
        List<ActiveNodeEntry> entries = activeRegistry.get(caster.getCasterId());
        if (entries != null) {
            entries.removeIf(entry -> entry.getNodeId().equals(nodeId));
        }
    }

    public static void unregisterAllForPlayer(UUID playerUuid) {
        if (playerUuid != null) {
            activeRegistry.remove(playerUuid);
        }
    }

    /**
     * SKILLタブの回路をスキャンし、ON_TICKトリガーノードを常駐実行として登録する。
     * ログイン時・回路保存時に呼ぶことを想定。呼ぶたびに一旦クリアしてから登録し直すので、
     * 「編集して回路からON_TICKを消した」場合も自然に反映される。
     */
    public static void scanAndRegisterResidentNodes(ServerPlayer player) {
        IMagicCaster caster = new PlayerCasterAdapter(player);
        unregisterAllForPlayer(caster.getCasterId());

        PlayerMagicData magicData = player.getData(ModAttachments.PLAYER_MAGIC_DATA);
        MagiculeCircuit skillCircuit = magicData.getCircuit(EditorTab.SKILL);
        if (skillCircuit == null) return;

        RuntimeMagicCircuit runtimeCircuit = MagicCompiler.compileCircuit(caster, skillCircuit);
        if (runtimeCircuit == null) return;

        for (AbstractMagicNode node : runtimeCircuit.getInstancedNodes().values()) {
            if (node.isTrigger() && "on_tick".equals(node.getTriggerType())) {
                registerActiveNode(caster, node.getId(), node, skillCircuit, runtimeCircuit, BalanceConfig.RESIDENT_NODE_INTERVAL_TICKS.get());
            }
        }
    }

    public static void onServerTick(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            List<ActiveNodeEntry> entries = activeRegistry.get(player.getUUID());
            if (entries == null || entries.isEmpty()) continue;

            List<ActiveNodeEntry> safeEntries = new ArrayList<>(entries);
            for (ActiveNodeEntry entry : safeEntries) {
                if (entry.shouldExecute()) {
                    entry.execute();
                }
            }
        }
    }

    public static void executeEventTrigger(IMagicCaster caster, EditorTab tab, String triggerNodeType, Map<String, Object> eventData) {
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
            }
        }
    }
}