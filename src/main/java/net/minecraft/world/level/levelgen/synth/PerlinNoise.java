package net.minecraft.world.level.levelgen.synth;

import java.util.Random;

public class PerlinNoise extends Synth {
   private ImprovedNoise[] noiseLevels;
   private int levels;

   public PerlinNoise(int var1) {
      this(new Random(), var1);
   }

   public PerlinNoise(Random var1, int var2) {
      this.levels = var2;
      this.noiseLevels = new ImprovedNoise[var2];

      for(int var3 = 0; var3 < var2; ++var3) {
         this.noiseLevels[var3] = new ImprovedNoise(var1);
      }

   }

   public double getValue(double var1, double var3) {
      double var5 = (double)0.0F;
      double var7 = (double)1.0F;

      for(int var9 = 0; var9 < this.levels; ++var9) {
         var5 += this.noiseLevels[var9].getValue(var1 * var7, var3 * var7) / var7;
         var7 /= (double)2.0F;
      }

      return var5;
   }

   public double getValue(double var1, double var3, double var5) {
      double var7 = (double)0.0F;
      double var9 = (double)1.0F;

      for(int var11 = 0; var11 < this.levels; ++var11) {
         var7 += this.noiseLevels[var11].getValue(var1 * var9, var3 * var9, var5 * var9) / var9;
         var9 /= (double)2.0F;
      }

      return var7;
   }

   public double[] getRegion(double[] var1, double var2, double var4, double var6, int var8, int var9, int var10, double var11, double var13, double var15) {
      if (var1 == null) {
         var1 = new double[var8 * var9 * var10];
      } else {
         for(int var17 = 0; var17 < var1.length; ++var17) {
            var1[var17] = (double)0.0F;
         }
      }

      double var20 = (double)1.0F;

      for(int var19 = 0; var19 < this.levels; ++var19) {
         this.noiseLevels[var19].add(var1, var2, var4, var6, var8, var9, var10, var11 * var20, var13 * var20, var15 * var20, var20);
         var20 /= (double)2.0F;
      }

      return var1;
   }

   public double[] getRegion(double[] var1, int var2, int var3, int var4, int var5, double var6, double var8, double var10) {
      return this.getRegion(var1, (double)var2, (double)10.0F, (double)var3, var4, 1, var5, var6, (double)1.0F, var8);
   }
}
