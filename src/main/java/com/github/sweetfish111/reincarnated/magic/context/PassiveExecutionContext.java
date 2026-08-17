package com.github.sweetfish111.reincarnated.magic.context;

/**
 * 「今実行中の処理が、プレイヤーの直接操作(詠唱キー押下)由来か、
 * 自動的な継続実行(常駐ノード・タイマーループ)由来か」を判定するスレッドローカルフラグ。
 * 魔素消費のスタイル判定(バースト/継続)がこれを見て、自動実行分を判定対象から除外する。
 */
public class PassiveExecutionContext {
    private static final ThreadLocal<Boolean> PASSIVE = ThreadLocal.withInitial(() -> false);

    public static boolean isPassive() {
        return PASSIVE.get();
    }

    public static void runAsPassive(Runnable action) {
        boolean previous = PASSIVE.get();
        PASSIVE.set(true);
        try {
            action.run();
        } finally {
            PASSIVE.set(previous);
        }
    }
}