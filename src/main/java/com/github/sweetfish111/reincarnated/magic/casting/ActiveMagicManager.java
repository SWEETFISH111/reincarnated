package com.github.sweetfish111.reincarnated.magic.casting;

import com.github.sweetfish111.reincarnated.circuit.EditorTab;
import com.github.sweetfish111.reincarnated.event.CalculationCapacityOverException;
import com.github.sweetfish111.reincarnated.event.MasoShortageException;
import com.github.sweetfish111.reincarnated.init.ModAttachments;
import com.github.sweetfish111.reincarnated.magic.compiler.MagicCompiler;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.MagicNode;
import com.github.sweetfish111.reincarnated.player.PlayerMagicData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class ActiveMagicManager {
    // プレイヤーのUUIDごとに、現在稼働しているアクティブ（常駐・パッシブ）ノードのマップを保持
    // 例: Key = プレイヤーUUID, Value = 稼働中のノードリスト
    private static final Map<UUID, List<ActiveNodeEntry>> activeRegistry = new ConcurrentHashMap<>();

    /**
     * 常駐ノードのエントリークラス
     */
    public static class ActiveNodeEntry {
        private final UUID nodeId;
        private final MagicNode nodeInstance;
        private final int intervalTicks; // スロットリング用（例: 5Tickに1回実行など）
        private int tickCounter = 0;

        public ActiveNodeEntry(UUID nodeId, MagicNode nodeInstance, int intervalTicks) {
            this.nodeId = nodeId;
            this.nodeInstance = nodeInstance;
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

        public void execute(ServerPlayer player) {
            try {
                // 実行用のコンテキストを生成（術者やレベル情報を内包）
                PlayerMagicData magicData = player.getData(ModAttachments.PLAYER_MAGIC_DATA);
                MagicContext context = new MagicContext(player, magicData.getCircuit(EditorTab.MAGIC), MagicCompiler.compileCircuit(magicData.getCircuit(EditorTab.MAGIC)));
                nodeInstance.execute(context);
            } catch (CalculationCapacityOverException c) {
                player.sendSystemMessage(Component.literal("《告》個体名" + player.getName() + "の演算容量が限界を超過。術式暴走が発生"));
                player.level().explode(player, player.getX(), player.getY(), player.getZ(), 10.0f, Level.ExplosionInteraction.TNT);
            } catch (MasoShortageException m) {
                player.sendSystemMessage(Component.literal("《告》個体名" + player.getName().getString() + "の魔素残量が低下。術式を維持できません"));
            }
        }

        public UUID getNodeId() {
            return nodeId;
        }
    }

    /**
     * プレイヤーが新しい常駐ノードを有効化したときに登録する
     */
    public static void registerActiveNode(ServerPlayer player, UUID nodeId, MagicNode node, int intervalTicks) {
        activeRegistry.computeIfAbsent(player.getUUID(), k -> new ArrayList<>())
                .removeIf(entry -> entry.getNodeId().equals(nodeId)); // 既存の重複を防ぐ

        activeRegistry.get(player.getUUID()).add(new ActiveNodeEntry(nodeId, node, intervalTicks));
    }

    /**
     * 常駐ノードを無効化したとき（またはログアウト時）に解除する
     */
    public static void unregisterActiveNode(ServerPlayer player, UUID nodeId) {
        List<ActiveNodeEntry> entries = activeRegistry.get(player.getUUID());
        if (entries != null) {
            entries.removeIf(entry -> entry.getNodeId().equals(nodeId));
        }
    }

    /**
     * プレイヤーごとの全アクティブノードをクリアする（死亡時やリログ時など）
     */
    public static void clearPlayer(UUID playerUuid) {
        activeRegistry.remove(playerUuid);
    }

    /**
     * 👑 サーバーの心臓部：毎Tickのイベントから呼び出す実行ループ
     */
    public static void onServerTick(ServerLevel level) {
        // 現在ワールドにいる全プレイヤーに対してアクティブノードを安全に処理
        for (ServerPlayer player : level.players()) {
            List<ActiveNodeEntry> entries = activeRegistry.get(player.getUUID());
            if (entries == null || entries.isEmpty()) continue;

            // イテレート中の安全性を考慮してコピーまたはそのまま回す
            for (ActiveNodeEntry entry : entries) {
                // スロットリング（間引き処理）の判定を挟むことでサーバー負荷を劇的に軽減！
                if (entry.shouldExecute()) {
                    entry.execute(player);
                }
            }
        }
    }
}
