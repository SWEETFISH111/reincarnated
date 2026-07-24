package com.github.sweetfish111.reincarnated.magic.nodes.math;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;

public class AndNode extends AbstractMagicNode {
    @Override
    public void execute(MagicContext context) {
        //なにもしない
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        boolean data1 = pullBoolean(0, context);
        boolean data2 = pullBoolean(1, context);
        return data1 && data2;
    }
}
