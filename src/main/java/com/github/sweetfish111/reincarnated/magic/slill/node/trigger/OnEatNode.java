package com.github.sweetfish111.reincarnated.magic.slill.node.trigger;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;

import java.util.UUID;

public class OnEatNode extends AbstractMagicNode {
    public OnEatNode(UUID id) {
        super(id);
        isTrigger = true;
        triggerType = "on_eat";
    }

    @Override
    public void execute(MagicContext context) {
        super.execute(context);
        pushExecute(context);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(portIndex, context);
        Object o = this.eventData.get("satietyLevel");
        if(o != null){
            return o;
        }
        return 0;
    }
}
