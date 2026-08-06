package com.github.sweetfish111.reincarnated.magic.nodes.control;

import com.github.sweetfish111.reincarnated.magic.caster.IMagicCaster;
import com.github.sweetfish111.reincarnated.magic.casting.CastingManager;
import com.github.sweetfish111.reincarnated.magic.casting.DelayCastingManager;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.magic.nodes.MagicNode;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.UUID;

public class DelayNode extends AbstractMagicNode {
    public DelayNode(UUID id) {
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
        super.execute(context);
        IMagicCaster caster = context.getCaster();
        if (caster == null) return;

        // ノードの設定パラメータから遅延ティック数を取り出す（例: デフォルト20Ticks = 1秒）
        double delaySeconds = pullDouble(1, context);
        int delayTicks = (int) (delaySeconds * 20);


        // CastingManagerの待合室にタスクを預け、ここで同期ループを抜ける（ここで処理がストップする）
        DelayCastingManager.scheduleDelay(caster, ((AbstractMagicNode) (this.getNextNode(0))).getId(), context, delayTicks);
    }

    @Override
    public void pushExecute(MagicContext context) {
        super.pushExecute(context);
    }
}
