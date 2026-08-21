package com.github.sweetfish111.reincarnated.magic;

public class OverLoadBuff {
    private double pendingMultiplier = 1.0;
    private boolean consumed = true;

    public void arm(double multiplier){
        pendingMultiplier = multiplier;
        consumed = false;
    }
    public double consumeIfAvailable(){
        if(consumed)return 1.0;
        consumed = true;
        return pendingMultiplier;
    }
}
