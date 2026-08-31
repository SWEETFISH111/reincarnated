package com.github.sweetfish111.reincarnated.magic.casting;

import com.github.sweetfish111.reincarnated.circuit.EditorTab;
import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.circuit.RuntimeMagicCircuit;
import com.github.sweetfish111.reincarnated.init.ReincarnatedAttachments;
import com.github.sweetfish111.reincarnated.magic.caster.IMagicCaster;
import com.github.sweetfish111.reincarnated.magic.caster.PlayerCasterAdapter;
import com.github.sweetfish111.reincarnated.magic.compiler.MagicCompiler;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.context.PassiveExecutionContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.player.PlayerMagicData;
import com.github.sweetfish111.reincarnated.config.BalanceConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

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
        private final double load;
        private int tickCounter = 0;

        public ActiveNodeEntry(UUID nodeId, AbstractMagicNode nodeInstance, MagiculeCircuit sourceCircuit,
                               RuntimeMagicCircuit runtimeCircuit, int intervalTicks, double load) {
            this.nodeId = nodeId;
            this.nodeInstance = nodeInstance;
            this.sourceCircuit = sourceCircuit;
            this.runtimeCircuit = runtimeCircuit;
            this.intervalTicks = Math.max(1, intervalTicks);
            this.load = load;
        }

        public boolean shouldExecute() {
            tickCounter++;
            if (tickCounter >= intervalTicks) { tickCounter = 0; return true; }
            return false;
        }

        public void execute() {
            PassiveExecutionContext.runAsPassive(() -> { // ★実行だけをpassive扱いで包む
                MagicContext context = new MagicContext(sourceCircuit, runtimeCircuit);
                nodeInstance.execute(context);
            });
        }

        public UUID getNodeId() { return nodeId; }
        public double getLoad() { return load; }
    }

    public static void registerActiveNode(IMagicCaster caster, UUID nodeId, AbstractMagicNode node,
                                          MagiculeCircuit sourceCircuit, RuntimeMagicCircuit runtimeCircuit,
                                          int intervalTicks, double load) {
        activeRegistry.computeIfAbsent(caster.getCasterId(), k -> new CopyOnWriteArrayList<>())
                .removeIf(entry -> entry.getNodeId().equals(nodeId));
        activeRegistry.get(caster.getCasterId()).add(new ActiveNodeEntry(nodeId, node, sourceCircuit, runtimeCircuit, intervalTicks, load));
    }

    public static double getComputeUsage(UUID playerId) {
        List<ActiveNodeEntry> entries = activeRegistry.get(playerId);
        if (entries == null) return 0.0;
        double sum = 0.0;
        for (ActiveNodeEntry entry : entries) sum += entry.getLoad();
        return sum;
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
        PlayerMagicData magicData = player.getData(ReincarnatedAttachments.PLAYER_MAGIC_DATA);
        IMagicCaster caster = new PlayerCasterAdapter(player);

        unregisterAllForPlayer(caster.getCasterId());

        MagiculeCircuit skillCircuit = magicData.getCircuit(EditorTab.SKILL);
        MagiculeCircuit magicCircuit = magicData.getCircuit(EditorTab.MAGIC);
        double maxCapacity = magicData.getMaxComputeCapacity();
        boolean anySkipped = false;

        resisterTickNodes(caster, skillCircuit, maxCapacity, anySkipped, player);
        resisterTickNodes(caster, magicCircuit, maxCapacity, anySkipped, player);
    }

    public static void resisterTickNodes(IMagicCaster caster, MagiculeCircuit circuit, double maxCapacity, boolean anySkipped, Player player){
        if (circuit == null) return;

        RuntimeMagicCircuit runtimeCircuit = MagicCompiler.compileCircuit(caster, circuit);
        if (runtimeCircuit == null) return;
        double usedCapacity = 0.0;

        for (AbstractMagicNode node : runtimeCircuit.getInstancedNodes().values()) {
            if (node.isTrigger() && "on_tick".equals(node.getTriggerType())) {
                double load = CastCostCalculator.calculateSubgraphComplexity(node); // ★前回話したクリティカルパス公式を再利用
                if (usedCapacity + load > maxCapacity) {
                    anySkipped = true;
                    continue; // 演算能力が足りず、この常駐術式は起動できない
                }
                usedCapacity += load;
                registerActiveNode(caster, node.getId(), node, circuit, runtimeCircuit,
                        BalanceConfig.RESIDENT_NODE_INTERVAL_TICKS.get(), load);
            }
        }

        if (anySkipped) {
            player.sendSystemMessage(Component.translatable("message.reincarnated.compute_capacity_exceeded"));
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
            MagiculeCircuit circuit = player.getData(ReincarnatedAttachments.PLAYER_MAGIC_DATA).getCircuit(tab);
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