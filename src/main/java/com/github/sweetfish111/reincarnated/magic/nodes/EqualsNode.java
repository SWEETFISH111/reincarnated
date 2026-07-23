package com.github.sweetfish111.reincarnated.magic.nodes;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;

public class EqualsNode extends AbstractMagicNode{
    @Override
    public void execute(MagicContext context) {
        //何もしない
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        Object data1 = pullData(0, context);
        Object data2 = pullData(1, context);
        return data1.equals(data2);
    }
}
