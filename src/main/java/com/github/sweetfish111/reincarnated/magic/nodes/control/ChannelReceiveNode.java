package com.github.sweetfish111.reincarnated.magic.nodes.control;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.magic.signal.SignalChannelManager;

import java.util.UUID;

/**
 * チャンネルに記憶されている最新の値を、その場で読み出す汎用センサーノード。
 * EXECを持たず、GET_MAX_MASO等と同じ「値ノード」として振る舞う。
 * まだ何も送信されていないチャンネルを読むとnullを返す(接続先のpullXXXが型ごとに0/nullへフォールバックする)。
 */
public class ChannelReceiveNode extends AbstractMagicNode {
    public ChannelReceiveNode(UUID id) {
        super(id);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(portIndex, context);
        int channel = (int) pullDouble(0, context);
        UUID casterId = context.getCaster().getCasterId();
        return SignalChannelManager.getValue(channel);
    }
}