package com.github.sweetfish111.reincarnated.magic.skill.node.action;

import com.github.sweetfish111.reincarnated.magic.casting.GainPenaltyTracker;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.magic.record.MasoAmount;

import java.util.UUID;

public class AddMasoNode extends AbstractMagicNode {
    private static final String PENALTY_GROUP = "ADD_MASO";

    public AddMasoNode(UUID id) {
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
        context.incrementAndCheck();
        MasoAmount maso = pullMaso(1, context);
        float rawAmount = (float) maso.amount();
        // 同一詠唱内で複製されたAddMasoノードに対する収穫逓減
        float grantedAmount = GainPenaltyTracker.applyAndGetDelta(context, PENALTY_GROUP, rawAmount);

        context.getCaster().addMaso(grantedAmount);
        context.getCaster().addTotalRegeneratedMaso(grantedAmount);
        pushExecute(context);
    }

}
