package com.github.sweetfish111.reincarnated.magic.nodes.control;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;

import java.util.UUID;

public class IfNode extends AbstractMagicNode {
    public IfNode(UUID id){
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
        if(pullBoolean(1,context)){
            pushExecute(0,context);
        }else {
            pushExecute(1, context);
        }
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        return null;
    }
}
