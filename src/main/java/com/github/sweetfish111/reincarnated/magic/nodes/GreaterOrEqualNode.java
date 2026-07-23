package com.github.sweetfish111.reincarnated.magic.nodes;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import net.minecraft.world.phys.Vec3;

public class GreaterOrEqualNode extends AbstractMagicNode{
    @Override
    public void execute(MagicContext context) {
        //なにもしない
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        double data1 = convertToComparableValue(pullData(0, context));
        double data2 = convertToComparableValue(pullData(1, context));
        return data1 >= data2;
    }

    private double convertToComparableValue(Object val) {
        if (val instanceof Number n) {
            return n.doubleValue();
        } else if (val instanceof Vec3 vec) {
            return vec.length(); // ベクトルの長さ（距離・勢い）を数値化
        }
        return 0.0;
    }
}
