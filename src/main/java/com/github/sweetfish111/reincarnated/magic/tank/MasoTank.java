package com.github.sweetfish111.reincarnated.magic.tank;

import com.github.sweetfish111.reincarnated.event.MasoShortageException;
import com.github.sweetfish111.reincarnated.world.LandMasoDensityData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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

    public double getShortfall(double amount) {
        return Math.max(0, amount - balance);
    }

    public double getBalance(){
        return balance;
    }

    public double getLimit(){return limit;}

    public void finalizeAndReturn(ServerLevel level, BlockPos pos){
        if(balance > 0){
            LandMasoDensityData.get(level).returnToLand(level, pos, balance);
            balance = 0;
        }
    }

    public CompoundTag saveToNBT(){
        CompoundTag rootTag = new CompoundTag();
        rootTag.putDouble("balance", this.balance);
        rootTag.putDouble("limit", this.limit);
        return rootTag;
    }

    public void loadFromNBT(CompoundTag rootTag){
        if(rootTag.contains("balance")){
            this.balance = rootTag.getDoubleOr("balance", 0);
        }
        if(rootTag.contains("limit")){
            this.limit = rootTag.getDoubleOr("limit", 0);
        }
    }
}
