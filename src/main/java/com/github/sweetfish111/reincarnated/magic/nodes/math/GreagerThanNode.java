package com.github.sweetfish111.reincarnated.magic.nodes.math;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.magic.record.MasoAmount;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class GreagerThanNode extends AbstractMagicNode {
    public GreagerThanNode(UUID id) {
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
        //なにもしない
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(portIndex, context);
        double data1 = convertToComparableValue(pullData(0, context));
        double data2 = convertToComparableValue(pullData(1, context));
        return data1 > data2;
    }

    private double convertToComparableValue(Object val) {
        if (val instanceof Number n) {
            return n.doubleValue();
        } else if (val instanceof Vec3 vec) {
            return vec.length(); // ベクトルの長さ（距離・勢い）を数値化
        } else if (val instanceof MasoAmount(double amount)){
            return amount;
        }
        return 0.0;
    }
}
