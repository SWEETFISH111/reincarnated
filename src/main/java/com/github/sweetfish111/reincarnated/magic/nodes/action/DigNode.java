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

    // 投入魔素量から算出した総掘削量(体積)予算を、幅×高さの断面積で割って奥行きに変換する。
    // 幅・高さは形状（アスペクト比）を決める純粋なNumberNode入力のまま残し、
    // 「どれだけ深く掘れるか」だけを投入魔素量が決める。
    private int computeDepth(int width, int height, MagicContext context) {
        double investedMaso = pullDouble(4, context);
        float availableMaso = context.getCaster().getMasoAmount();

        MasoInvestmentScaling.EffectResult effectResult =
                MasoInvestmentScaling.computeEffect(BASECOST, (float) investedMaso, availableMaso);
        masoCost = effectResult.masoCost();

        int crossSection = Math.max(1, width * height);
        return Math.max(1, Math.round(effectResult.effectAmount() / crossSection));
    }

    @Override
    public void execute(MagicContext context) {
        Vec3 posVec = pullVector3(1, context);
        int width  = Math.max(1, (int) Math.round(pullDouble(2, context)));
        int height = Math.max(1, (int) Math.round(pullDouble(3, context)));
        int depth  = computeDepth(width, height, context);

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
        double investedMaso = pullDouble(4, context);
        float availableMaso = context.getCaster().getMasoAmount();
        MasoInvestmentScaling.EffectResult effectResult =
                MasoInvestmentScaling.computeEffect(BASECOST, (float) investedMaso, availableMaso);
        return effectResult.masoCost();
    }
}
