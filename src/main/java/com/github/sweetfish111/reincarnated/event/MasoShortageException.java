package com.github.sweetfish111.reincarnated.event;

public class MasoShortageException extends RuntimeException{
    private final float requiredAmount;
    private final float currentAmount;

    public MasoShortageException(float requiredAmount, float currentAmount){
        super(String.format("魔素が不足しています (要求: %.1f, 所持: %.1f)", requiredAmount, currentAmount));
        this.requiredAmount = requiredAmount;
        this.currentAmount = currentAmount;
    }
    public float getRequiredAmount() {
        return requiredAmount;
    }

    public float getCurrentAmount() {
        return currentAmount;
    }

    public float getDeficit() {
        return requiredAmount - currentAmount;
    }
}
