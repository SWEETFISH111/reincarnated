package com.github.sweetfish111.reincarnated.magic.slill.node.trigger;

import com.github.sweetfish111.reincarnated.magic.DamageAmount;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import net.minecraft.world.entity.Entity;

import java.util.UUID;


public class OnDamageNode extends AbstractMagicNode {
    public OnDamageNode(UUID id) {
        super(id);
        this.isTrigger = true;
        this.triggerType = "on_damage";
    }

    @Override
    public void execute(MagicContext context) {
        super.execute(context);
        pushExecute(context);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        context.incrementAndCheck();
        Object raw = this.eventData.get("damageAmount");
        double value = (raw instanceof Number n) ? n.doubleValue() : 0.0;
        return new DamageAmount(value);
    }
}
