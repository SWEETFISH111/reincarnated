package com.github.sweetfish111.reincarnated.magic;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.client.screen.ICycleButtonValue;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.player.PlayerMagicData;
import com.github.sweetfish111.reincarnated.world.LandMasoDensityData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public enum PoolType implements ICycleButtonValue {
    PLAYER{
        @Override
        public Component getDisplayName() {
            return Component.translatable("gui.reincarnated.pooltype_player");
        }

        @Override
        public double consumeMaso(MagicContext context, double request) {
            float masoAmount = context.getCaster().getMasoAmount();
            if(masoAmount < request){
                context.getCaster().consumeMaso(masoAmount);
                return masoAmount;
            }else{
                context.getCaster().consumeMaso((float) request);
                return request;
            }
        }
    },
    LAND{
        @Override
        public Component getDisplayName() {
            return Component.translatable("gui.reincarnated.pooltype_land");
        }

        @Override
        public double consumeMaso(MagicContext context, double request) {
            ServerLevel level = context.getCaster().getCasterLevel();
            BlockPos pos = BlockPos.containing(context.getCaster().getCasterPosition());
            return LandMasoDensityData.get(level).consumeCurrent(level, pos, request);
        }
    };

    public abstract double consumeMaso(MagicContext context, double request);
}
