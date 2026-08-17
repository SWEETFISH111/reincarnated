package com.github.sweetfish111.reincarnated.magic.nodes.action;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.magic.signal.SignalChannelManager;

import java.util.UUID;

/**
 * チャンネルへ値を送信する汎用ノード。
 * ・payloadポート(ANY)を繋げば、その値をチャンネルに記憶させる(ChannelReceiveNodeが後から読める)
 * ・payloadポートを繋がなくても、送信自体が「合図」として機能する(OnChannelSignalNodeの待機を解除する)
 */
public class ChannelSendNode extends AbstractMagicNode {
    public ChannelSendNode(UUID id) {
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
        context.incrementAndCheck();
        int channel = (int) pullDouble(1, context);
        UUID casterId = context.getCaster().getCasterId();

        if (isInputConnected(2)) { // payloadポートに何か繋がっていれば値を記憶
            Object payload = pullData(2, context);
            SignalChannelManager.setValue(casterId, channel, payload);
        }

        SignalChannelManager.sendSignal(casterId, channel); // 合図として待機中のOnChannelSignalNodeを起こす
        pushExecute(context);
    }
}