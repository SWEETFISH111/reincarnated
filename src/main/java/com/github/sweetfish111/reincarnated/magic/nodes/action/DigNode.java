package com.github.sweetfish111.reincarnated.magic.nodes.action;

import com.github.sweetfish111.reincarnated.magic.casting.MasoInvestmentScaling;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class DigNode extends AbstractMagicNode {
    float BASECOST = 0.7f;

    public DigNode(UUID id) {
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
        Vec3 posVec = pullVector3(1, context);
        int width  = Math.max(1, (int) Math.round(pullDouble(2, context)));
        int height = Math.max(1, (int) Math.round(pullDouble(3, context)));
        int depth  = Math.max(1, (int) Math.round(pullDouble(4, context)));

        // 掘削量（幅×高さ×奥行き）に応じてコストをスケール
        float availableMaso = context.getCaster().getMasoAmount();

        MasoInvestmentScaling.CostResult costResult =
                MasoInvestmentScaling.computeCost(BASECOST, ((float) (width * height * depth)), availableMaso);
        masoCost = costResult.cost();
        super.execute(context);

        if (posVec == null) return;

        // 視線方向の水平成分を「奥行き」の進行方向として使う
        Vec3 lookVec = context.getCaster().getCasterEntity().getLookAngle();
        Vec3 forward = new Vec3(lookVec.x, 0, lookVec.z);
        if (forward.lengthSqr() < 1.0E-4) {
            // 真上/真下を向いていた場合のフォールバック（外積の縮退防止）
            forward = new Vec3(0, 0, 1);
        }
        forward = forward.normalize();

        // 奥行き方向に直交する「幅」方向（右手系）と「高さ」方向
        Vec3 right = forward.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 up = new Vec3(0, 1, 0);

        int halfWidth = width / 2;
        int halfHeight = height / 2;

        for (int d = 0; d < depth; d++) {
            for (int h = 0; h < height; h++) {
                for (int w = 0; w < width; w++) {
                    context.incrementAndCheck(); // 演算容量チェック（範囲が暴走的に大きい場合の術式暴走対策）

                    int wOffset = w - halfWidth;
                    int hOffset = h - halfHeight;

                    Vec3 targetPos = posVec
                            .add(forward.scale(d))
                            .add(right.scale(wOffset))
                            .add(up.scale(hOffset));
                    BlockPos pos = BlockPos.containing(targetPos);

                    if (context.getLevel().isLoaded(pos)) {
                        context.getLevel().destroyBlock(pos, true, context.getCaster().getCasterEntity());
                    }
                }
            }
        }
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(portIndex, context);
        int width  = Math.max(1, (int) Math.round(pullDouble(2, context)));
        int height = Math.max(1, (int) Math.round(pullDouble(3, context)));
        int depth  = Math.max(1, (int) Math.round(pullDouble(4, context)));
        float availableMaso = context.getCaster().getMasoAmount();
        MasoInvestmentScaling.CostResult costResult =
                MasoInvestmentScaling.computeCost(BASECOST, ((float) (width * height * depth)), availableMaso);
        return costResult.cost();
    }
}