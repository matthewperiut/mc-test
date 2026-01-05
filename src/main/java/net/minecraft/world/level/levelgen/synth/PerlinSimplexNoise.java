package net.minecraft.world.level.levelgen.synth;

import java.util.Random;

public class PerlinSimplexNoise extends Synth {
   private SimplexNoise[] noiseLevels;
   private int levels;

   public PerlinSimplexNoise(int var1) {
      this(new Random(), var1);
   }

   public PerlinSimplexNoise(Random var1, int var2) {
      this.levels = var2;
      this.noiseLevels = new SimplexNoise[var2];

      for(int var3 = 0; var3 < var2; ++var3) {
         this.noiseLevels[var3] = new SimplexNoise(var1);
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

   public double[] getRegion(double[] var1, double var2, double var4, int var6, int var7, double var8, double var10, double var12) {
      return this.getRegion(var1, var2, var4, var6, var7, var8, var10, var12, (double)0.5F);
   }

   public double[] getRegion(double[] var1, double var2, double var4, int var6, int var7, double var8, double var10, double var12, double var14) {
      var8 /= (double)1.5F;
      var10 /= (double)1.5F;
      if (var1 != null && var1.length >= var6 * var7) {
         for(int var16 = 0; var16 < var1.length; ++var16) {
            var1[var16] = (double)0.0F;
         }
      } else {
         var1 = new double[var6 * var7];
      }

      double var23 = (double)1.0F;
      double var18 = (double)1.0F;

      for(int var20 = 0; var20 < this.levels; ++var20) {
         this.noiseLevels[var20].add(var1, var2, var4, var6, var7, var8 * var18, var10 * var18, 0.55 / var23);
         var18 *= var12;
         var23 *= var14;
      }

      return var1;
   }

   public double[] getRegion(double[] var1, double var2, double var4, double var6, int var8, int var9, int var10, double var11, double var13, double var15) {
      var11 /= (double)1.5F;
      var13 /= (double)1.5F;
      if (var1 == null) {
         var1 = new double[var8 * var9 * var10];
      } else {
         for(int var17 = 0; var17 < var1.length; ++var17) {
            var1[var17] = (double)0.0F;
         }
      }

      double var22 = (double)1.0F;

      for(int var19 = 0; var19 < this.levels; ++var19) {
         this.noiseLevels[var19].add(var1, var2, var4, var6, var8, var9, var10, var11 * var22, var13 * var22, var15 * var22, 0.55 / var22);
         var22 *= (double)0.5F;
      }

      return var1;
   }
}
