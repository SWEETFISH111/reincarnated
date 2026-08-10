package com.github.sweetfish111.reincarnated.circuit;

import com.github.sweetfish111.reincarnated.magic.compiler.MagicCompiler;

import java.util.Map;
import java.util.WeakHashMap;

public class CircuitCompileCache {
    // サーバーはシングルスレッド tick 前提（ModNetworkingもenqueueWork経由なので main thread）
    private static final Map<MagiculeCircuit, CompiledCircuitGraph> CACHE = new WeakHashMap<>();

    public static CompiledCircuitGraph getOrCompile(MagiculeCircuit circuit) {
        CompiledCircuitGraph cached = CACHE.get(circuit);
        if (cached != null) return cached;

        CompiledCircuitGraph fresh = MagicCompiler.compileGraph(circuit);
        CACHE.put(circuit, fresh);
        return fresh;
    }

    // 開発中の挙動確認やデバッグコマンド用に、明示クリアも用意しておくと便利
    public static void invalidate(MagiculeCircuit circuit) {
        CACHE.remove(circuit);
    }
}