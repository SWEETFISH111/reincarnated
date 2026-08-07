package com.github.sweetfish111.reincarnated.magic.nodes.control;

import com.github.sweetfish111.reincarnated.magic.casting.TimerCastingManager;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;

import java.util.UUID;

public class RepeatNode extends AbstractMagicNode {

    public RepeatNode(UUID id) {
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
        super.execute(context);

        // 入力データの取得
        int totalCount = (int) pullDouble(1, context);
        double intervalTime = pullDouble(2, context);

        // ローカル変数の初期化（コンテキスト側でマップの初期化が必要な場合があります）
        context.setNodeLocalVariable(this.id, 0, 0);

        AbstractMagicNode nextNode = (AbstractMagicNode) this.getNextNode(0);
        if (nextNode != null) {
            pushExecute(0, context);
        }

        totalCount--;

        if (nextNode != null && totalCount >= 1) {
            TimerCastingManager.registerTimer(
                    nextNode.getId(),
                    this.id,
                    context,
                    (int) (intervalTime * 20),
                    (int) (intervalTime * 20),
                    totalCount - 1
            );
        }
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(portIndex, context);
        Object val = context.getNodeLocalVariable(this.id, 0);
        if (val instanceof Number num) {
            return num.doubleValue();
        }
        return 0.0;
    }
}