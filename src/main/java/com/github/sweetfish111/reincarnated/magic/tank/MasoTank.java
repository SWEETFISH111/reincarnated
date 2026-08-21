package com.github.sweetfish111.reincarnated.magic.tank;

import com.github.sweetfish111.reincarnated.event.MasoShortageException;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class MasoTank {
    private double balance;
    private double limit;

    public MasoTank(double limit){
        this.limit = limit;
    }

    public double deposit(double amount){
        double roomLeft = limit - balance;
        if (amount <= roomLeft) {
            balance += amount;
            return 0; // 超過なし
        } else {
            balance = limit;
            return amount - roomLeft; // 超過分を呼び出し元に返す
        }
    }

    public void withdraw(double amount){
        if (balance < amount) {
            double shortage = amount - balance;
            balance = 0; // 残高は使い切る
            throw new MasoShortageException((float) amount, (float) (amount - shortage));
        }
        balance -= amount;
    }

    public double getBalance(){
        return balance;
    }

    public void finalizeAndReturn(ServerLevel level, BlockPos pos){
        if(balance > 0){
           // LandMasoDensityData.get(level).returnToLand(level, pos, balance)
            balance = 0;
        }
    }
}
