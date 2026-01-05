package net.minecraft.world.level.levelgen.synth;

public abstract class Synth {
   public Synth() {
   }

   public abstract double getValue(double var1, double var3);

   public double[] create(int var1, int var2) {
      double[] var3 = new double[var1 * var2];

      for(int var4 = 0; var4 < var2; ++var4) {
         for(int var5 = 0; var5 < var1; ++var5) {
            var3[var5 + var4 * var1] = this.getValue((double)var5, (double)var4);
         }
      }

      return var3;
   }
}
