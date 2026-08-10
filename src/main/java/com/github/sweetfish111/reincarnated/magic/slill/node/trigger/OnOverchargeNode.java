package com.github.sweetfish111.reincarnated.magic.slill.node.trigger;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;

import java.util.UUID;

public class OnOverchargeNode extends AbstractMagicNode {
    public OnOverchargeNode(UUID id) {
        super(id);
        isTrigger = true;
        triggerType = "on_overcharge";
    }

    @Override
    public void execute(MagicContext context) {
        super.execute(context);
        pushExecute(0, context);
    }
}
