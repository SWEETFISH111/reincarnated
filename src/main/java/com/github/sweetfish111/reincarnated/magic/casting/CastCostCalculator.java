package com.github.sweetfish111.reincarnated.magic.casting;

import com.github.sweetfish111.reincarnated.circuit.CircuitCompileCache;
import com.github.sweetfish111.reincarnated.circuit.CompiledCircuitGraph;
import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.config.BalanceConfig;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.magic.nodes.MagicNode;

import java.util.*;

public class CastCostCalculator {

    public static int calculateCastTimeTicks(MagiculeCircuit circuit) {
        CompiledCircuitGraph graph = CircuitCompileCache.getOrCompile(circuit);
        Map<UUID, AbstractMagicNode> nodes = graph.getInstancedNodes();
        if (nodes.isEmpty()) return 0;

        GraphMetrics metrics = computeGraphMetrics(nodes);
        return (int) Math.ceil(applyCostFormula(metrics.maxDepth(), metrics.nodeCount()));
    }

    /**
     * 特定のトリガーノードから到達可能な部分グラフだけを対象に、
     * 詠唱時間コストと全く同じ公式で複雑さを算出する。
     * 常駐術式(ON_TICK等)の演算負荷判定に使う。
     */
    public static double calculateSubgraphComplexity(AbstractMagicNode triggerNode) {
        Map<UUID, AbstractMagicNode> reachable = collectReachableNodes(triggerNode);
        GraphMetrics metrics = computeGraphMetrics(reachable);
        return applyCostFormula(metrics.maxDepth(), metrics.nodeCount());
    }

    public static double applyCostFormula(double depth, int nodeCount) {
        return BalanceConfig.BASE_CAST_TICKS.get()
                + BalanceConfig.CAST_DEPTH_COEFFICIENT.get() * Math.pow(depth, BalanceConfig.CAST_DEPTH_EXPONENT.get())
                + BalanceConfig.CAST_WIDTH_COEFFICIENT.get() * Math.pow(nodeCount, BalanceConfig.CAST_WIDTH_EXPONENT.get());
    }

    private record GraphMetrics(double maxDepth, int nodeCount) {}

    private static Map<UUID, AbstractMagicNode> collectReachableNodes(AbstractMagicNode start) {
        Map<UUID, AbstractMagicNode> reachable = new HashMap<>();
        Deque<AbstractMagicNode> frontier = new ArrayDeque<>();
        frontier.add(start);
        reachable.put(start.getId(), start);

        while (!frontier.isEmpty()) {
            AbstractMagicNode node = frontier.poll();

            for (List<MagicNode> targets : node.getOutputConnections().values()) {
                for (MagicNode target : targets) {
                    if (target instanceof AbstractMagicNode t && !reachable.containsKey(t.getId())) {
                        reachable.put(t.getId(), t);
                        frontier.add(t);
                    }
                }
            }
            for (MagicNode source : node.getDataInputSourceNodes()) {
                if (source instanceof AbstractMagicNode s && !reachable.containsKey(s.getId())) {
                    reachable.put(s.getId(), s);
                    frontier.add(s);
                }
            }
        }
        return reachable;
    }

    /** 与えられたノード集合"の中だけ"でEXEC辺の逆引き＋クリティカルパス深さを計算する */
    private static GraphMetrics computeGraphMetrics(Map<UUID, AbstractMagicNode> nodes) {
        Map<UUID, List<AbstractMagicNode>> execPredecessors = new HashMap<>();
        for (AbstractMagicNode node : nodes.values()) {
            for (List<MagicNode> targets : node.getOutputConnections().values()) {
                for (MagicNode target : targets) {
                    if (target instanceof AbstractMagicNode t && nodes.containsKey(t.getId())) {
                        execPredecessors.computeIfAbsent(t.getId(), k -> new ArrayList<>()).add(node);
                    }
                }
            }
        }

        Map<UUID, Double> depthCache = new HashMap<>();
        double maxDepth = 0.0;
        for (AbstractMagicNode node : nodes.values()) {
            maxDepth = Math.max(maxDepth,
                    computeDepth(node, nodes, execPredecessors, depthCache, new HashSet<>()));
        }
        return new GraphMetrics(maxDepth, nodes.size());
    }

    private static double computeDepth(
            AbstractMagicNode node,
            Map<UUID, AbstractMagicNode> scope,
            Map<UUID, List<AbstractMagicNode>> execPredecessors,
            Map<UUID, Double> cache,
            Set<UUID> visiting
    ) {
        UUID id = node.getId();
        Double cached = cache.get(id);
        if (cached != null) return cached;

        if (!visiting.add(id)) {
            return node.getCastCost();
        }

        double maxPredDepth = 0.0;

        List<AbstractMagicNode> execPreds = execPredecessors.get(id);
        if (execPreds != null) {
            for (AbstractMagicNode pred : execPreds) {
                maxPredDepth = Math.max(maxPredDepth, computeDepth(pred, scope, execPredecessors, cache, visiting));
            }
        }

        for (MagicNode source : node.getDataInputSourceNodes()) {
            if (source instanceof AbstractMagicNode sourceNode && scope.containsKey(sourceNode.getId())) {
                maxPredDepth = Math.max(maxPredDepth, computeDepth(sourceNode, scope, execPredecessors, cache, visiting));
            }
        }

        visiting.remove(id);
        double depth = node.getCastCost() + maxPredDepth;
        cache.put(id, depth);
        return depth;
    }
}