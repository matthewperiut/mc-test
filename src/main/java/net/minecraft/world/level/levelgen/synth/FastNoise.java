package net.minecraft.world.level.levelgen.synth;

import java.util.Random;

public class FastNoise {
   private byte[][] noiseMaps;
   private int levels;

   public FastNoise(int var1) {
      this(new Random(), var1);
   }

   public FastNoise(Random var1, int var2) {
      this.levels = var2;
      this.noiseMaps = new byte[var2][1048576];

      for(int var3 = 0; var3 < var2; ++var3) {
         var1.nextBytes(this.noiseMaps[var3]);
      }

   }

   public double[] getRegion(double[] var1, double var2, double var4, double var6, int var8, int var9, int var10, double var11, double var13, double var15) {
      if (var1 == null) {
         var1 = new double[var8 * var9 * var10];
      } else {
         for(int var17 = 0; var17 < var1.length; ++var17) {
            var1[var17] = (double)0.0F;
         }
      }

      double var64 = (double)1.0F;
      int var19 = 487211441;
      short var20 = 21771;

      for(int var21 = 0; var21 < this.levels; ++var21) {
         byte[] var22 = this.noiseMaps[var21];
         int var23 = 0;

         for(int var24 = 0; var24 < var10; ++var24) {
            double var25 = (var6 + (double)var24) * var15;
            int var27 = (int)var25;
            if (var25 < (double)var27) {
               --var27;
            }

            int var28 = (int)((var25 - (double)var27) * (double)65536.0F);

            for(int var29 = 0; var29 < var9; ++var29) {
               double var30 = (var4 + (double)var29) * var13;
               int var32 = (int)var30;
               if (var30 < (double)var32) {
                  --var32;
               }

               int var33 = (int)((var30 - (double)var32) * (double)65536.0F);

               for(int var34 = 0; var34 < var8; ++var34) {
                  double var35 = (var2 + (double)var34) * var11;
                  int var37 = (int)var35;
                  if (var35 < (double)var37) {
                     --var37;
                  }

                  int var38 = (int)((var35 - (double)var37) * (double)65536.0F);
                  int var39 = (var37 + 0) * var19;
                  int var40 = (var37 + 1) * var19;
                  int var41 = var32 + 0;
                  int var42 = var32 + 1;
                  int var43 = var27 + 0;
                  int var44 = var27 + 1;
                  int var45 = (var39 + var41) * var20;
                  int var46 = (var40 + var41) * var20;
                  int var47 = (var39 + var42) * var20;
                  int var48 = (var40 + var42) * var20;
                  byte var49 = var22[var45 + var43 & 1048575];
                  byte var50 = var22[var46 + var43 & 1048575];
                  byte var51 = var22[var47 + var43 & 1048575];
                  byte var52 = var22[var48 + var43 & 1048575];
                  byte var53 = var22[var45 + var44 & 1048575];
                  byte var54 = var22[var46 + var44 & 1048575];
                  byte var55 = var22[var47 + var44 & 1048575];
                  byte var56 = var22[var48 + var44 & 1048575];
                  int var57 = var49 + ((var50 - var49) * var38 >> 16);
                  int var58 = var51 + ((var52 - var51) * var38 >> 16);
                  int var59 = var53 + ((var54 - var53) * var38 >> 16);
                  int var60 = var55 + ((var56 - var55) * var38 >> 16);
                  int var61 = var57 + ((var58 - var57) * var33 >> 16);
                  int var62 = var59 + ((var60 - var59) * var33 >> 16);
                  int var63 = var61 + ((var62 - var61) * var28 >> 16);
                  int var10001 = var23++;
                  var1[var10001] += (double)var63 * var64;
               }
            }
         }

         var64 /= (double)2.0F;
         var11 *= (double)2.0F;
         var13 *= (double)2.0F;
         var15 *= (double)2.0F;
      }

      return var1;
   }
}
