package com.github.sweetfish111.reincarnated.magic.nodes.math;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;

public class ModuloNode extends AbstractMagicNode {
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
