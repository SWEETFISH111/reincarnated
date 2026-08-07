package com.github.sweetfish111.reincarnated.magic.nodes.action;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class DigNode extends AbstractMagicNode {
    public DigNode(UUID id) {
        super(id);
        masoCost = 1;
    }

    @Override
    public void execute(MagicContext context) {
        super.execute(context);
        Vec3 posVec = pullVector3(1, context);

        if(posVec == null) return;

        BlockPos pos = BlockPos.containing(posVec);
// レベルやプロテクション（世界の理）を考慮しつつブロックを破壊し、ドロップ品を生成する
        if (context.getLevel().isLoaded(pos)) {
            // ブロックを破壊してドロップアイテムをスポーンさせる（バニラの破壊処理を安全に呼び出す）
            context.getLevel().destroyBlock(pos, true, context.getCaster().getCasterEntity());
        }
    }
}
