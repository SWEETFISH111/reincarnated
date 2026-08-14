package com.github.sweetfish111.reincarnated.magic.casting;

import com.github.sweetfish111.reincarnated.circuit.CircuitCompileCache;
import com.github.sweetfish111.reincarnated.circuit.CompiledCircuitGraph;
import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.magic.nodes.MagicNode;

import java.util.*;

/**
 * 詠唱時間コストの算出。
 * ノード数の単純合計ではなく「クリティカルパス深さ D」＋「総ノード数Nによる幅コスト」の
 * 二項で構成する。
 *
 *   castTimeTicks = baseCastTicks + k_t * D^1.3 + k_w * N^0.8
 *
 * D: トリガーから終端までの最長経路（EXEC辺・データ依存辺の両方を辺として扱う）の重み合計
 * N: 回路内の全ノード数（並列複製への抑止＝先日話した「3並列3倍」対策の時間側の分担）
 */
public class CastCostCalculator {

    private static final double BASE_CAST_TICKS = 5.0;
    private static final double K_T = 2.0;
    private static final double DEPTH_EXPONENT = 1.3;
    private static final double K_W = 0.5;
    private static final double WIDTH_EXPONENT = 0.8;

    public static int calculateCastTimeTicks(MagiculeCircuit circuit) {
        CompiledCircuitGraph graph = CircuitCompileCache.getOrCompile(circuit);
        Map<UUID, AbstractMagicNode> nodes = graph.getInstancedNodes();
        if (nodes.isEmpty()) return 0;

        // EXEC辺は発信元ノード側にしか記録されていないので、逆引き用に先に構築する
        Map<UUID, List<AbstractMagicNode>> execPredecessors = new HashMap<>();
        for (AbstractMagicNode node : nodes.values()) {
            for (List<MagicNode> targets : node.getOutputConnections().values()) {
                for (MagicNode target : targets) {
                    if (target instanceof AbstractMagicNode targetNode) {
                        execPredecessors
                                .computeIfAbsent(targetNode.getId(), k -> new ArrayList<>())
                                .add(node);
                    }
                }
            }
        }

        Map<UUID, Double> depthCache = new HashMap<>();
        double maxDepth = 0.0;
        for (AbstractMagicNode node : nodes.values()) {
            maxDepth = Math.max(maxDepth,
                    computeDepth(node, execPredecessors, depthCache, new HashSet<>()));
        }

        int totalNodes = nodes.size();
        double castTime = BASE_CAST_TICKS
                + K_T * Math.pow(maxDepth, DEPTH_EXPONENT)
                + K_W * Math.pow(totalNodes, WIDTH_EXPONENT);

        return (int) Math.ceil(castTime);
    }

    /**
     * 深さ = 自分の重み(castCost) + 依存先（EXECの前段 or データ供給元）の中で最大の深さ
     */
    private static double computeDepth(
            AbstractMagicNode node,
            Map<UUID, List<AbstractMagicNode>> execPredecessors,
            Map<UUID, Double> cache,
            Set<UUID> visiting
    ) {
        UUID id = node.getId();
        Double cached = cache.get(id);
        if (cached != null) return cached;

        // 循環参照の防御（コンパイル段階で弾かれる想定だが念のため）
        if (!visiting.add(id)) {
            return node.getCastCost();
        }

        double maxPredDepth = 0.0;

        List<AbstractMagicNode> execPreds = execPredecessors.get(id);
        if (execPreds != null) {
            for (AbstractMagicNode pred : execPreds) {
                maxPredDepth = Math.max(maxPredDepth,
                        computeDepth(pred, execPredecessors, cache, visiting));
            }
        }

        for (MagicNode source : node.getDataInputSourceNodes()) {
            if (source instanceof AbstractMagicNode sourceNode) {
                maxPredDepth = Math.max(maxPredDepth,
                        computeDepth(sourceNode, execPredecessors, cache, visiting));
            }
        }

        visiting.remove(id);
        double depth = node.getCastCost() + maxPredDepth;
        cache.put(id, depth);
        return depth;
    }
}