package com.github.sweetfish111.reincarnated.magic.skill.node.trigger;

import com.github.sweetfish111.reincarnated.magic.record.PowerGapAmount;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;

import java.util.UUID;

public class OnAttackStronger extends AbstractMagicNode {
    public OnAttackStronger(UUID id) {
        super(id);
        isTrigger = true;
        triggerType = "on_attack_stronger";
    }

    @Override
    public void execute(MagicContext context) {
        super.execute(context);
        pushExecute(context);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(portIndex, context);
        Object rawData = this.eventData.get("power_gap");
        double d = (rawData instanceof Number n) ? n.doubleValue() : 0.0;
        return new PowerGapAmount(d);
    }
}
