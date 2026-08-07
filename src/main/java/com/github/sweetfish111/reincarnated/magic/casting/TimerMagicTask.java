package com.github.sweetfish111.reincarnated.magic.casting;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;

import java.util.UUID;

public class TimerMagicTask {
    private final UUID nextNodeId;
    private final UUID repeatNodeId;
    private final MagicContext context;
    private int remainingTicks;
    private final int intervalTicks;
    private int remainingCount;
    private int currentLoopIndex = 1;

    public TimerMagicTask(UUID nextNodeId, UUID repeatNodeId, MagicContext context, int remainingTicks, int intervalTicks, int remainingCount) {
        this.nextNodeId = nextNodeId;
        this.repeatNodeId = repeatNodeId;
        this.context = context;
        this.remainingTicks = remainingTicks;
        this.intervalTicks = intervalTicks;
        this.remainingCount = remainingCount;
    }

    public TimerMagicTask(UUID nextNodeId, UUID repeatNodeId, MagicContext context, int remainingTicks, int intervalTicks){
        this.nextNodeId = nextNodeId;
        this.repeatNodeId = repeatNodeId;
        this.context = context;
        this.remainingTicks = remainingTicks;
        this.intervalTicks = intervalTicks;
        this.remainingCount = -1;
    }

    public boolean tick(){
        if(remainingTicks > 0){
            remainingTicks--;
        }
        return remainingTicks <= 0;
    }

    public void resetTimer(){
        this.remainingTicks = this.intervalTicks;
        this.remainingCount--;
        this.currentLoopIndex++;
    }

    public boolean hasMore(){
        if(remainingCount >= 0){
            return this.remainingCount > 0;
        }else if(remainingCount < 0){
            return true;
        }
        return false;
    }

    public UUID getNextNodeId(){
        return nextNodeId;
    }

    public MagicContext getContext(){
        return context;
    }
    public UUID getRepeatNodeId(){return this.repeatNodeId;}
    public int getCurrentLoopIndex(){return this.currentLoopIndex;}
    public int getRemainingCount(){return this.remainingCount;}
}
