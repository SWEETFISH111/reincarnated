package com.github.sweetfish111.reincarnated.magic.context;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ExecutionTrace {
    private final Map<UUID, Integer> nodeExecutionCounts = new HashMap<>();

    private double totalMagiculeCost = 0.0;

    public void recordExecution(UUID nodeId, double baseCost, int computeCost){
        nodeExecutionCounts.put(nodeId, nodeExecutionCounts.getOrDefault(nodeId, 0) + 1);
        this.totalMagiculeCost += baseCost;
    }

    public int getExecutionCount(UUID nodeId){
        return nodeExecutionCounts.getOrDefault(nodeId, 0);
    }

    public double getTotalMagiculeCost(){
        return totalMagiculeCost;
    }
}
