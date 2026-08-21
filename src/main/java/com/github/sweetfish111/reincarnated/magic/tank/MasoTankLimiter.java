package com.github.sweetfish111.reincarnated.magic.tank;

import com.github.sweetfish111.reincarnated.config.BalanceConfig;
import com.github.sweetfish111.reincarnated.player.PlayerMagicData;

public class MasoTankLimiter {

    public static double getMasoTankCapacity(PlayerMagicData magicData){
        return BalanceConfig.TANK_BASE_CAPACITY.get()
                + magicData.getMaxComputeCapacity() * BalanceConfig.TANK_CAPACITY_PER_COMPUTE.get();
    }
}
