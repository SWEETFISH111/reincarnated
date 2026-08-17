package com.github.sweetfish111.reincarnated.magic.signal;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.circuit.RuntimeMagicCircuit;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.control.OnChannelSignalNode;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * チャンネルはプレイヤーごとではなく、サーバー全体で共有されるグローバルな名前空間。
 * これにより原理的には「誰の回路が、どのチャンネルを掴んでいるか」を知っていれば
 * 他プレイヤーの回路に信号や値を送り込める(将来の魔法システムハッキング機能の土台)。
 * 現時点ではアクセス制御は一切無い＝チャンネル番号を知っていれば誰でも読み書きできる。
 */
public class SignalChannelManager {

    private record PendingWait(UUID ownerId, OnChannelSignalNode continuationNode, MagiculeCircuit circuit, RuntimeMagicCircuit runtimeCircuit) {}
    private record ChannelValue(Object value, UUID lastWriterId) {}

    private static final Map<Integer, List<PendingWait>> waiters = new ConcurrentHashMap<>();
    private static final Map<Integer, ChannelValue> values = new ConcurrentHashMap<>();

    public static void registerWait(UUID ownerId, int channel, OnChannelSignalNode node,
                                    MagiculeCircuit circuit, RuntimeMagicCircuit runtimeCircuit) {
        waiters.computeIfAbsent(channel, k -> new CopyOnWriteArrayList<>())
                .add(new PendingWait(ownerId, node, circuit, runtimeCircuit));
    }

    /** senderIdは将来の検知・ログ用に受け取っておくが、送信先の絞り込みには使わない(グローバル送信) */
    public static void sendSignal(UUID senderId, int channel) {
        List<PendingWait> pending = waiters.remove(channel);
        if (pending == null) return;

        for (PendingWait wait : pending) {
            MagicContext context = new MagicContext(wait.circuit(), wait.runtimeCircuit());
            wait.continuationNode().resumeAfterSignal(context);
        }
    }

    public static void setValue(UUID senderId, int channel, Object value) {
        values.put(channel, new ChannelValue(value, senderId));
    }

    public static Object getValue(int channel) {
        ChannelValue v = values.get(channel);
        return v != null ? v.value() : null;
    }

    /** このチャンネルに最後に書き込んだプレイヤー。将来の侵入検知・出所表示に使う想定 */
    public static UUID getLastWriter(int channel) {
        ChannelValue v = values.get(channel);
        return v != null ? v.lastWriterId() : null;
    }

    /**
     * ログアウト時の後始末。待機中の継続処理(そのプレイヤーの回路インスタンスへの参照)だけを除去する。
     * 値ストアは他プレイヤーが読み取り中の可能性があるため、書き込み元の退室では消さない。
     */
    public static void clearAllForCaster(UUID casterId) {
        for (List<PendingWait> list : waiters.values()) {
            list.removeIf(w -> w.ownerId().equals(casterId));
        }
    }
}