package com.github.sweetfish111.reincarnated.magic.nodes.trigger;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;

public class EventKeyOneNode extends AbstractMagicNode {
    @Override
    public void execute(MagicContext context) {
        pushExecute(context);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context){
        return null;
    }
}
