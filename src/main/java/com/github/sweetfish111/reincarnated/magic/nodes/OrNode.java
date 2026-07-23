package com.github.sweetfish111.reincarnated.magic.nodes;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;

public class OrNode extends AbstractMagicNode{
    @Override
    public void execute(MagicContext context) {
        //何もしない
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        boolean data1 = pullBoolean(0, context);
        boolean data2 = pullBoolean(1, context);
        return data1 || data2;
    }
}
