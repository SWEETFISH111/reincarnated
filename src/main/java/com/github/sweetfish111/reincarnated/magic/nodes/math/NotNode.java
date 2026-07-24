package com.github.sweetfish111.reincarnated.magic.nodes.math;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;

public class NotNode extends AbstractMagicNode {
    @Override
    public void execute(MagicContext context) {
        //何もしない
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        boolean rawData = pullBoolean(0, context);
        return !rawData;
    }
}
