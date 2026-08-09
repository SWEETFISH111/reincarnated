package com.github.sweetfish111.reincarnated.magic.slill.node.trigger;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.magic.XpAmount;

import java.util.UUID;

public class OnXpPickupNode extends AbstractMagicNode {

    public OnXpPickupNode(UUID id){
        super(id);
        this.isTrigger = true;
        this.triggerType = "on_xp_pickup";
    }

    @Override
    public void execute(MagicContext context) {
        context.incrementAndCheck();
        pushExecute(context);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        context.incrementAndCheck();
        Object raw = this.eventData.get("xp_amount");
        double value = (raw instanceof Number n) ? n.doubleValue() : 0.0;
        return new XpAmount(value);
    }
}
