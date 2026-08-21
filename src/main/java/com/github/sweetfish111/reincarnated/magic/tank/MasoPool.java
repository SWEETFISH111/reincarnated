package com.github.sweetfish111.reincarnated.magic.tank;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;

public enum MasoPool {
     Player{
        @Override
        public double getAvailable(MagicContext context) {
            return context.getCaster().getMasoAmount();
        }

        @Override
        public void consume(MagicContext context, float amount) {
            context.getCaster().consumeMaso(amount);
        }

         @Override
         public String getDisplayName() {
             return "player";
         }

         @Override
         public MasoPool getNext() {
             return MasoPool.Player;
         }
     };

    public abstract double getAvailable(MagicContext context);
    public abstract void consume(MagicContext context, float amount);
    public abstract String getDisplayName();
    public abstract MasoPool getNext();
}
