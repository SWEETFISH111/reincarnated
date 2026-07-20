package com.github.sweetfish111.reincarnated.magic.nodes;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;

public class ReturnCaster extends AbstractMagicNode{
    @Override
    public void execute(MagicContext context) {
        //なにもしない
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        return context.getCaster();
    }
}
