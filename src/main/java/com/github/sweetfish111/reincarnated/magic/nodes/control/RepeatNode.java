package com.github.sweetfish111.reincarnated.magic.nodes.control;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;

import java.util.UUID;

public class RepeatNode extends AbstractMagicNode {
    public RepeatNode(UUID id){
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
        System.out.println("before");
        double rawCount = (int)pullDouble(1, context);
        int count = (rawCount <= 0) ? 1 : (int) Math.round(rawCount);
        count = Math.min(count, 20);
        for(int i = 0; i < count; i++){
            context.setNodeLocalVariable(this.id, 0, (double) i);
            executeOutputPort(0, context);
            System.out.println("executing " + i);
        }
        System.out.println("finish");
        pushExecute(2, context);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        Object val = context.getNodeLocalVariable(this.id, 0);
        if (val instanceof Number num) {
            return num.doubleValue();
        }
        return 0.0;
    }
}
