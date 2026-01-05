package net.minecraft.world.level.levelgen.synth;

import java.util.Random;

public class SimplexNoise {
   private static int[][] grad3 = new int[][]{{1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0}, {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1}, {0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1}};
   private int[] p;
   public double scale;
   public double xo;
   public double yo;
   public double zo;
   private static final double F2 = (double)0.5F * (Math.sqrt((double)3.0F) - (double)1.0F);
   private static final double G2 = ((double)3.0F - Math.sqrt((double)3.0F)) / (double)6.0F;

   public SimplexNoise() {
      this(new Random());
   }

   public SimplexNoise(Random var1) {
      this.p = new int[512];
      this.xo = var1.nextDouble() * (double)256.0F;
      this.yo = var1.nextDouble() * (double)256.0F;
      this.zo = var1.nextDouble() * (double)256.0F;

      for(int var2 = 0; var2 < 256; this.p[var2] = var2++) {
      }

      for(int var5 = 0; var5 < 256; ++var5) {
         int var3 = var1.nextInt(256 - var5) + var5;
         int var4 = this.p[var5];
         this.p[var5] = this.p[var3];
         this.p[var3] = var4;
         this.p[var5 + 256] = this.p[var5];
      }

   }

   private static int fastfloor(double var0) {
      return var0 > (double)0.0F ? (int)var0 : (int)var0 - 1;
   }

   private static double dot(int[] var0, double var1, double var3) {
      return (double)var0[0] * var1 + (double)var0[1] * var3;
   }

   private static double dot(int[] var0, double var1, double var3, double var5) {
      return (double)var0[0] * var1 + (double)var0[1] * var3 + (double)var0[2] * var5;
   }

   public double getValue(double var1, double var3) {
      double var11 = (double)0.5F * (Math.sqrt((double)3.0F) - (double)1.0F);
      double var13 = (var1 + var3) * var11;
      int var15 = fastfloor(var1 + var13);
      int var16 = fastfloor(var3 + var13);
      double var17 = ((double)3.0F - Math.sqrt((double)3.0F)) / (double)6.0F;
      double var19 = (double)(var15 + var16) * var17;
      double var21 = (double)var15 - var19;
      double var23 = (double)var16 - var19;
      double var25 = var1 - var21;
      double var27 = var3 - var23;
      byte var29;
      byte var30;
      if (var25 > var27) {
         var29 = 1;
         var30 = 0;
      } else {
         var29 = 0;
         var30 = 1;
      }

      double var31 = var25 - (double)var29 + var17;
      double var33 = var27 - (double)var30 + var17;
      double var35 = var25 - (double)1.0F + (double)2.0F * var17;
      double var37 = var27 - (double)1.0F + (double)2.0F * var17;
      int var39 = var15 & 255;
      int var40 = var16 & 255;
      int var41 = this.p[var39 + this.p[var40]] % 12;
      int var42 = this.p[var39 + var29 + this.p[var40 + var30]] % 12;
      int var43 = this.p[var39 + 1 + this.p[var40 + 1]] % 12;
      double var44 = (double)0.5F - var25 * var25 - var27 * var27;
      double var5;
      if (var44 < (double)0.0F) {
         var5 = (double)0.0F;
      } else {
         var44 *= var44;
         var5 = var44 * var44 * dot(grad3[var41], var25, var27);
      }

      double var46 = (double)0.5F - var31 * var31 - var33 * var33;
      double var7;
      if (var46 < (double)0.0F) {
         var7 = (double)0.0F;
      } else {
         var46 *= var46;
         var7 = var46 * var46 * dot(grad3[var42], var31, var33);
      }

      double var48 = (double)0.5F - var35 * var35 - var37 * var37;
      double var9;
      if (var48 < (double)0.0F) {
         var9 = (double)0.0F;
      } else {
         var48 *= var48;
         var9 = var48 * var48 * dot(grad3[var43], var35, var37);
      }

      return (double)70.0F * (var5 + var7 + var9);
   }

   public double getValue(double var1, double var3, double var5) {
      double var17 = (var1 + var3 + var5) * 0.3333333333333333;
      int var19 = fastfloor(var1 + var17);
      int var20 = fastfloor(var3 + var17);
      int var21 = fastfloor(var5 + var17);
      double var24 = (double)(var19 + var20 + var21) * 0.16666666666666666;
      double var26 = (double)var19 - var24;
      double var28 = (double)var20 - var24;
      double var30 = (double)var21 - var24;
      double var32 = var1 - var26;
      double var34 = var3 - var28;
      double var36 = var5 - var30;
      byte var38;
      byte var39;
      byte var40;
      byte var41;
      byte var42;
      byte var43;
      if (var32 >= var34) {
         if (var34 >= var36) {
            var38 = 1;
            var39 = 0;
            var40 = 0;
            var41 = 1;
            var42 = 1;
            var43 = 0;
         } else if (var32 >= var36) {
            var38 = 1;
            var39 = 0;
            var40 = 0;
            var41 = 1;
            var42 = 0;
            var43 = 1;
         } else {
            var38 = 0;
            var39 = 0;
            var40 = 1;
            var41 = 1;
            var42 = 0;
            var43 = 1;
         }
      } else if (var34 < var36) {
         var38 = 0;
         var39 = 0;
         var40 = 1;
         var41 = 0;
         var42 = 1;
         var43 = 1;
      } else if (var32 < var36) {
         var38 = 0;
         var39 = 1;
         var40 = 0;
         var41 = 0;
         var42 = 1;
         var43 = 1;
      } else {
         var38 = 0;
         var39 = 1;
         var40 = 0;
         var41 = 1;
         var42 = 1;
         var43 = 0;
      }

      double var44 = var32 - (double)var38 + 0.16666666666666666;
      double var46 = var34 - (double)var39 + 0.16666666666666666;
      double var48 = var36 - (double)var40 + 0.16666666666666666;
      double var50 = var32 - (double)var41 + 0.3333333333333333;
      double var52 = var34 - (double)var42 + 0.3333333333333333;
      double var54 = var36 - (double)var43 + 0.3333333333333333;
      double var56 = var32 - (double)1.0F + (double)0.5F;
      double var58 = var34 - (double)1.0F + (double)0.5F;
      double var60 = var36 - (double)1.0F + (double)0.5F;
      int var62 = var19 & 255;
      int var63 = var20 & 255;
      int var64 = var21 & 255;
      int var65 = this.p[var62 + this.p[var63 + this.p[var64]]] % 12;
      int var66 = this.p[var62 + var38 + this.p[var63 + var39 + this.p[var64 + var40]]] % 12;
      int var67 = this.p[var62 + var41 + this.p[var63 + var42 + this.p[var64 + var43]]] % 12;
      int var68 = this.p[var62 + 1 + this.p[var63 + 1 + this.p[var64 + 1]]] % 12;
      double var69 = 0.6 - var32 * var32 - var34 * var34 - var36 * var36;
      double var7;
      if (var69 < (double)0.0F) {
         var7 = (double)0.0F;
      } else {
         var69 *= var69;
         var7 = var69 * var69 * dot(grad3[var65], var32, var34, var36);
      }

      double var71 = 0.6 - var44 * var44 - var46 * var46 - var48 * var48;
      double var9;
      if (var71 < (double)0.0F) {
         var9 = (double)0.0F;
      } else {
         var71 *= var71;
         var9 = var71 * var71 * dot(grad3[var66], var44, var46, var48);
      }

      double var73 = 0.6 - var50 * var50 - var52 * var52 - var54 * var54;
      double var11;
      if (var73 < (double)0.0F) {
         var11 = (double)0.0F;
      } else {
         var73 *= var73;
         var11 = var73 * var73 * dot(grad3[var67], var50, var52, var54);
      }

      double var75 = 0.6 - var56 * var56 - var58 * var58 - var60 * var60;
      double var13;
      if (var75 < (double)0.0F) {
         var13 = (double)0.0F;
      } else {
         var75 *= var75;
         var13 = var75 * var75 * dot(grad3[var68], var56, var58, var60);
      }

      return (double)32.0F * (var7 + var9 + var11 + var13);
   }

   public void add(double[] var1, double var2, double var4, int var6, int var7, double var8, double var10, double var12) {
      int var14 = 0;

      for(int var15 = 0; var15 < var6; ++var15) {
         double var16 = (var2 + (double)var15) * var8 + this.xo;

         for(int var18 = 0; var18 < var7; ++var18) {
            double var19 = (var4 + (double)var18) * var10 + this.yo;
            double var27 = (var16 + var19) * F2;
            int var29 = fastfloor(var16 + var27);
            int var30 = fastfloor(var19 + var27);
            double var31 = (double)(var29 + var30) * G2;
            double var33 = (double)var29 - var31;
            double var35 = (double)var30 - var31;
            double var37 = var16 - var33;
            double var39 = var19 - var35;
            byte var41;
            byte var42;
            if (var37 > var39) {
               var41 = 1;
               var42 = 0;
            } else {
               var41 = 0;
               var42 = 1;
            }

            double var43 = var37 - (double)var41 + G2;
            double var45 = var39 - (double)var42 + G2;
            double var47 = var37 - (double)1.0F + (double)2.0F * G2;
            double var49 = var39 - (double)1.0F + (double)2.0F * G2;
            int var51 = var29 & 255;
            int var52 = var30 & 255;
            int var53 = this.p[var51 + this.p[var52]] % 12;
            int var54 = this.p[var51 + var41 + this.p[var52 + var42]] % 12;
            int var55 = this.p[var51 + 1 + this.p[var52 + 1]] % 12;
            double var56 = (double)0.5F - var37 * var37 - var39 * var39;
            double var21;
            if (var56 < (double)0.0F) {
               var21 = (double)0.0F;
            } else {
               var56 *= var56;
               var21 = var56 * var56 * dot(grad3[var53], var37, var39);
            }

            double var58 = (double)0.5F - var43 * var43 - var45 * var45;
            double var23;
            if (var58 < (double)0.0F) {
               var23 = (double)0.0F;
            } else {
               var58 *= var58;
               var23 = var58 * var58 * dot(grad3[var54], var43, var45);
            }

            double var60 = (double)0.5F - var47 * var47 - var49 * var49;
            double var25;
            if (var60 < (double)0.0F) {
               var25 = (double)0.0F;
            } else {
               var60 *= var60;
               var25 = var60 * var60 * dot(grad3[var55], var47, var49);
            }

            int var10001 = var14++;
            var1[var10001] += (double)70.0F * (var21 + var23 + var25) * var12;
         }
      }

   }

   public void add(double[] var1, double var2, double var4, double var6, int var8, int var9, int var10, double var11, double var13, double var15, double var17) {
      int var19 = 0;

      for(int var20 = 0; var20 < var8; ++var20) {
         double var21 = (var2 + (double)var20) * var11 + this.xo;

         for(int var23 = 0; var23 < var10; ++var23) {
            double var24 = (var6 + (double)var23) * var15 + this.zo;

            for(int var26 = 0; var26 < var9; ++var26) {
               double var27 = (var4 + (double)var26) * var13 + this.yo;
               double var39 = (var21 + var27 + var24) * 0.3333333333333333;
               int var41 = fastfloor(var21 + var39);
               int var42 = fastfloor(var27 + var39);
               int var43 = fastfloor(var24 + var39);
               double var46 = (double)(var41 + var42 + var43) * 0.16666666666666666;
               double var48 = (double)var41 - var46;
               double var50 = (double)var42 - var46;
               double var52 = (double)var43 - var46;
               double var54 = var21 - var48;
               double var56 = var27 - var50;
               double var58 = var24 - var52;
               byte var60;
               byte var61;
               byte var62;
               byte var63;
               byte var64;
               byte var65;
               if (var54 >= var56) {
                  if (var56 >= var58) {
                     var60 = 1;
                     var61 = 0;
                     var62 = 0;
                     var63 = 1;
                     var64 = 1;
                     var65 = 0;
                  } else if (var54 >= var58) {
                     var60 = 1;
                     var61 = 0;
                     var62 = 0;
                     var63 = 1;
                     var64 = 0;
                     var65 = 1;
                  } else {
                     var60 = 0;
                     var61 = 0;
                     var62 = 1;
                     var63 = 1;
                     var64 = 0;
                     var65 = 1;
                  }
               } else if (var56 < var58) {
                  var60 = 0;
                  var61 = 0;
                  var62 = 1;
                  var63 = 0;
                  var64 = 1;
                  var65 = 1;
               } else if (var54 < var58) {
                  var60 = 0;
                  var61 = 1;
                  var62 = 0;
                  var63 = 0;
                  var64 = 1;
                  var65 = 1;
               } else {
                  var60 = 0;
                  var61 = 1;
                  var62 = 0;
                  var63 = 1;
                  var64 = 1;
                  var65 = 0;
               }

               double var66 = var54 - (double)var60 + 0.16666666666666666;
               double var68 = var56 - (double)var61 + 0.16666666666666666;
               double var70 = var58 - (double)var62 + 0.16666666666666666;
               double var72 = var54 - (double)var63 + 0.3333333333333333;
               double var74 = var56 - (double)var64 + 0.3333333333333333;
               double var76 = var58 - (double)var65 + 0.3333333333333333;
               double var78 = var54 - (double)1.0F + (double)0.5F;
               double var80 = var56 - (double)1.0F + (double)0.5F;
               double var82 = var58 - (double)1.0F + (double)0.5F;
               int var84 = var41 & 255;
               int var85 = var42 & 255;
               int var86 = var43 & 255;
               int var87 = this.p[var84 + this.p[var85 + this.p[var86]]] % 12;
               int var88 = this.p[var84 + var60 + this.p[var85 + var61 + this.p[var86 + var62]]] % 12;
               int var89 = this.p[var84 + var63 + this.p[var85 + var64 + this.p[var86 + var65]]] % 12;
               int var90 = this.p[var84 + 1 + this.p[var85 + 1 + this.p[var86 + 1]]] % 12;
               double var91 = 0.6 - var54 * var54 - var56 * var56 - var58 * var58;
               double var29;
               if (var91 < (double)0.0F) {
                  var29 = (double)0.0F;
               } else {
                  var91 *= var91;
                  var29 = var91 * var91 * dot(grad3[var87], var54, var56, var58);
               }

               double var93 = 0.6 - var66 * var66 - var68 * var68 - var70 * var70;
               double var31;
               if (var93 < (double)0.0F) {
                  var31 = (double)0.0F;
               } else {
                  var93 *= var93;
                  var31 = var93 * var93 * dot(grad3[var88], var66, var68, var70);
               }

               double var95 = 0.6 - var72 * var72 - var74 * var74 - var76 * var76;
               double var33;
               if (var95 < (double)0.0F) {
                  var33 = (double)0.0F;
               } else {
                  var95 *= var95;
                  var33 = var95 * var95 * dot(grad3[var89], var72, var74, var76);
               }

               double var97 = 0.6 - var78 * var78 - var80 * var80 - var82 * var82;
               double var35;
               if (var97 < (double)0.0F) {
                  var35 = (double)0.0F;
               } else {
                  var97 *= var97;
                  var35 = var97 * var97 * dot(grad3[var90], var78, var80, var82);
               }

               int var10001 = var19++;
               var1[var10001] += (double)32.0F * (var29 + var31 + var33 + var35) * var17;
            }
         }
      }

   }
}
