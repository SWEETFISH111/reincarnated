package com.github.sweetfish111.reincarnated.blockentity;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.magic.tank.MasoTank;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import static com.github.sweetfish111.reincarnated.blockentity.ReincarnatedBlockEntities.MAGIC_CIRCLE_ENTITY;

public class MagicCircleEntity extends BlockEntity {
    private final MasoTank masoTank = new MasoTank(100);
    private final MagiculeCircuit circuit = new MagiculeCircuit();
    private final SimpleContainer inventory = new SimpleContainer(5);

    public MagicCircleEntity(BlockPos pos, BlockState state){
        super(MAGIC_CIRCLE_ENTITY.get(), pos, state);
    }

    public MasoTank getMasoTank(){return this.masoTank;}
    public MagiculeCircuit getCircuit(){return this.circuit;}
    public SimpleContainer getInventory(){return this.inventory;}

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("maso_tank", CompoundTag.CODEC, masoTank.saveToNBT());
        output.store("circuit", CompoundTag.CODEC, circuit.saveToNBT());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("maso_tank", CompoundTag.CODEC)
                .ifPresent(this.masoTank::loadFromNBT);
        input.read("circuit", CompoundTag.CODEC)
                .ifPresent(this.circuit::loadFromNBT);
    }
}
