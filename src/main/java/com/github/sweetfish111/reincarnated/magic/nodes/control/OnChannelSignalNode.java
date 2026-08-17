package com.github.sweetfish111.reincarnated.magic.nodes.control;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.magic.signal.SignalChannelManager;

import java.util.UUID;

/**
 * 指定チャンネルへの信号が来るまで、その場で回路の実行を一時停止するノード。
 * 信号を受け取ると、このノードの後続(EXEC接続先)へ実行が引き継がれる。
 */
public class OnChannelSignalNode extends AbstractMagicNode {
    public OnChannelSignalNode(UUID id) {
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
        int channel = (int) pullDouble(1, context);
        UUID casterId = context.getCaster().getCasterId();

        SignalChannelManager.registerWait(casterId, channel, this, context.getCircuit(), context.getRuntimeCircuit());
        // ここでは何もしない。pushExecute()は信号受信時にresumeAfterSignal()経由で呼ばれる。
    }

    /** SignalChannelManagerから信号受信時に呼ばれる再開処理 */
    public void resumeAfterSignal(MagicContext context) {
        pushExecute(context);
    }
}