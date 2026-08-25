package com.github.sweetfish111.reincarnated.magic.nodes.math;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;

import java.util.UUID;

public class EqualsNode extends AbstractMagicNode {
    public EqualsNode(UUID id) {
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
        //何もしない
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(portIndex, context);
        Object data1 = pullData(0, context);
        Object data2 = pullData(1, context);
        if(data1 != null && data2 != null){
            return data1.equals(data2);
        }
        if(data1 == null && data2 == null){
            return true;
        }
        return false;
    }
}
