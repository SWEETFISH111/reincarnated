package com.github.sweetfish111.reincarnated.magic.slill.node.trigger;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;

import java.util.UUID;

public class OnKillNode extends AbstractMagicNode {
    public OnKillNode(UUID id) {
        super(id);
        isTrigger = true;
        triggerType = "on_kill";
    }

    @Override
    public void execute(MagicContext context) {
        super.execute(context);
        pushExecute(context);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(portIndex, context);
        return this.eventData.get("killScore");
    }
}
