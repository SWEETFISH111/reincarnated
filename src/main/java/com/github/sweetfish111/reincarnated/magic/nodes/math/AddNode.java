package com.github.sweetfish111.reincarnated.magic.nodes.math;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.magic.record.MasoAmount;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class AddNode extends AbstractMagicNode {
    public AddNode(UUID id) {
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
        //なにもしない
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(portIndex, context);
        Object valA = pullData(0, context);
        Object valB = pullData(1, context);

        if (valA instanceof Number a && valB instanceof Number b) {
            return a.doubleValue() + b.doubleValue();
        } else if (valA instanceof Vec3 a && valB instanceof Vec3 b) {
            return a.add(b);
        }else if(valA instanceof MasoAmount && valB instanceof MasoAmount){
            return new MasoAmount(((MasoAmount) valA).amount() + ((MasoAmount) valB).amount());
        }
        return 0;
    }
}
