package com.github.sweetfish111.reincarnated.event;

public class CalculationCapacityOverException extends RuntimeException{
    private final int limitCapacity;
    private final int requiredCapacity;

    public CalculationCapacityOverException(int limitCapacity, int requiredCapacity) {
        super(String.format("演算容量が限界を超過しました (許容: %d, 要求: %d)", limitCapacity, requiredCapacity));
        this.limitCapacity = limitCapacity;
        this.requiredCapacity = requiredCapacity;
    }

    public int getLimitCapacity() {
        return limitCapacity;
    }

    public int getRequiredCapacity() {
        return requiredCapacity;
    }

    public int getOverloadAmount() {
        return requiredCapacity - limitCapacity;
    }
}
