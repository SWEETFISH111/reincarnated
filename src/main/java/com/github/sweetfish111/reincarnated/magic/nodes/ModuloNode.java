package com.github.sweetfish111.reincarnated.magic.nodes;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;

public class ModuloNode extends AbstractMagicNode{
    @Override
    public void execute(MagicContext context) {
        //なにもしない
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        double a = pullDouble(0, context);
        double b = pullDouble(1, context);
        return (b != 0) ? (a % b) : 0;
    }
}
