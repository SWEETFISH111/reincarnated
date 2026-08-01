package com.github.sweetfish111.reincarnated.magic.casting;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import net.minecraft.server.level.ServerPlayer;
import java.util.UUID;

public class DelayedMagicTask {
    private final ServerPlayer player;
    private final AbstractMagicNode currentNode; // 次に実行すべきノードのUUID
    private final MagicContext context;
    private int remainingTicks;    // 残り待機ティック数

    public DelayedMagicTask(ServerPlayer player, AbstractMagicNode currentNode, MagicContext context, int delayTicks) {
        this.player = player;
        this.currentNode = currentNode;
        this.context = context;
        this.remainingTicks = delayTicks;
    }

    // 毎Tick呼ばれ、タイマーを減らす。0になったらtrueを返す
    public boolean tick() {
        remainingTicks--;
        return remainingTicks <= 0;
    }

    public ServerPlayer getPlayer() { return player; }
    public AbstractMagicNode getCurrentNode(){return currentNode;}
    public MagicContext getContext() { return context; }
}