package com.github.sweetfish111.reincarnated.magic.nodes.trigger;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.slill.node.action.AbsorptionNode;

import java.util.UUID;

public class OnSlotEnableNode extends AbsorptionNode {
    public OnSlotEnableNode(UUID id) {
        super(id);
        isTrigger = true;
        triggerType = "on_slot_enabled";
    }

    @Override
    public void execute(MagicContext context) {
        super.execute(context);
        pushExecute(0, context);
    }

}
