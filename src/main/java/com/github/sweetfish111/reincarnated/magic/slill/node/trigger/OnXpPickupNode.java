package com.github.sweetfish111.reincarnated.magic.slill.node.trigger;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;

import java.util.UUID;

public class OnXpPickupNode extends AbstractMagicNode {

    public OnXpPickupNode(UUID id){
        super(id);
        this.isTrigger = true;
    }

    @Override
    public void execute(MagicContext context) {
        context.incrementAndCheck();
        pushExecute(context);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        context.incrementAndCheck();
        return this.eventData.get("xp_amount");
    }
}
