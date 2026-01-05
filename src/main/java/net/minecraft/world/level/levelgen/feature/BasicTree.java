package net.minecraft.world.level.levelgen.feature;

import java.util.Random;
import net.minecraft.world.level.Level;
import util.Mth;

public class BasicTree extends Feature {
   static final byte[] axisConversionArray = new byte[]{2, 0, 0, 1, 2, 1};
   Random rnd = new Random();
   Level thisLevel;
   int[] origin = new int[]{0, 0, 0};
   int height = 0;
   int trunkHeight;
   double trunkHeightScale = 0.618;
   double branchDensity = (double)1.0F;
   double branchSlope = 0.381;
   double widthScale = (double)1.0F;
   double foliageDensity = (double)1.0F;
   int trunkWidth = 1;
   int heightVariance = 12;
   int foliageHeight = 4;
   int[][] foliageCoords;

   public BasicTree() {
   }

   void prepare() {
      this.trunkHeight = (int)((double)this.height * this.trunkHeightScale);
      if (this.trunkHeight >= this.height) {
         this.trunkHeight = this.height - 1;
      }

      int var1 = (int)(1.382 + Math.pow(this.foliageDensity * (double)this.height / (double)13.0F, (double)2.0F));
      if (var1 < 1) {
         var1 = 1;
      }

      int[][] var2 = new int[var1 * this.height][4];
      int var3 = this.origin[1] + this.height - this.foliageHeight;
      int var4 = 1;
      int var5 = this.origin[1] + this.trunkHeight;
      int var6 = var3 - this.origin[1];
      var2[0][0] = this.origin[0];
      var2[0][1] = var3;
      var2[0][2] = this.origin[2];
      var2[0][3] = var5;
      --var3;

      while(var6 >= 0) {
         int var7 = 0;
         float var8 = this.treeShape(var6);
         if (var8 < 0.0F) {
            --var3;
            --var6;
         } else {
            for(double var9 = (double)0.5F; var7 < var1; ++var7) {
               double var11 = this.widthScale * (double)var8 * ((double)this.rnd.nextFloat() + 0.328);
               double var13 = (double)this.rnd.nextFloat() * (double)2.0F * 3.14159;
               int var15 = (int)(var11 * Math.sin(var13) + (double)this.origin[0] + var9);
               int var16 = (int)(var11 * Math.cos(var13) + (double)this.origin[2] + var9);
               int[] var17 = new int[]{var15, var3, var16};
               int[] var18 = new int[]{var15, var3 + this.foliageHeight, var16};
               if (this.checkLine(var17, var18) == -1) {
                  int[] var19 = new int[]{this.origin[0], this.origin[1], this.origin[2]};
                  double var20 = Math.sqrt(Math.pow((double)Math.abs(this.origin[0] - var17[0]), (double)2.0F) + Math.pow((double)Math.abs(this.origin[2] - var17[2]), (double)2.0F));
                  double var22 = var20 * this.branchSlope;
                  if ((double)var17[1] - var22 > (double)var5) {
                     var19[1] = var5;
                  } else {
                     var19[1] = (int)((double)var17[1] - var22);
                  }

                  if (this.checkLine(var19, var17) == -1) {
                     var2[var4][0] = var15;
                     var2[var4][1] = var3;
                     var2[var4][2] = var16;
                     var2[var4][3] = var19[1];
                     ++var4;
                  }
               }
            }

            --var3;
            --var6;
         }
      }

      this.foliageCoords = new int[var4][4];
      System.arraycopy(var2, 0, this.foliageCoords, 0, var4);
   }

   void crossection(int var1, int var2, int var3, float var4, byte var5, int var6) {
      int var7 = (int)((double)var4 + 0.618);
      byte var8 = axisConversionArray[var5];
      byte var9 = axisConversionArray[var5 + 3];
      int[] var10 = new int[]{var1, var2, var3};
      int[] var11 = new int[]{0, 0, 0};
      int var12 = -var7;
      int var13 = -var7;

      for(var11[var5] = var10[var5]; var12 <= var7; ++var12) {
         var11[var8] = var10[var8] + var12;
         var13 = -var7;

         while(var13 <= var7) {
            double var15 = Math.sqrt(Math.pow((double)Math.abs(var12) + (double)0.5F, (double)2.0F) + Math.pow((double)Math.abs(var13) + (double)0.5F, (double)2.0F));
            if (var15 > (double)var4) {
               ++var13;
            } else {
               var11[var9] = var10[var9] + var13;
               int var14 = this.thisLevel.getTile(var11[0], var11[1], var11[2]);
               if (var14 != 0 && var14 != 18) {
                  ++var13;
               } else {
                  this.thisLevel.setTileNoUpdate(var11[0], var11[1], var11[2], var6);
                  ++var13;
               }
            }
         }
      }

   }

   float treeShape(int var1) {
      if ((double)var1 < (double)((float)this.height) * 0.3) {
         return -1.618F;
      } else {
         float var2 = (float)this.height / 2.0F;
         float var3 = (float)this.height / 2.0F - (float)var1;
         float var4;
         if (var3 == 0.0F) {
            var4 = var2;
         } else if (Math.abs(var3) >= var2) {
            var4 = 0.0F;
         } else {
            var4 = (float)Math.sqrt(Math.pow((double)Math.abs(var2), (double)2.0F) - Math.pow((double)Math.abs(var3), (double)2.0F));
         }

         var4 *= 0.5F;
         return var4;
      }
   }

   float foliageShape(int var1) {
      if (var1 >= 0 && var1 < this.foliageHeight) {
         return var1 != 0 && var1 != this.foliageHeight - 1 ? 3.0F : 2.0F;
      } else {
         return -1.0F;
      }
   }

   void foliageCluster(int var1, int var2, int var3) {
      int var4 = var2;

      for(int var5 = var2 + this.foliageHeight; var4 < var5; ++var4) {
         float var6 = this.foliageShape(var4 - var2);
         this.crossection(var1, var4, var3, var6, (byte)1, 18);
      }

   }

   void limb(int[] var1, int[] var2, int var3) {
      int[] var4 = new int[]{0, 0, 0};
      byte var5 = 0;

      byte var6;
      for(var6 = 0; var5 < 3; ++var5) {
         var4[var5] = var2[var5] - var1[var5];
         if (Math.abs(var4[var5]) > Math.abs(var4[var6])) {
            var6 = var5;
         }
      }

      if (var4[var6] != 0) {
         byte var7 = axisConversionArray[var6];
         byte var8 = axisConversionArray[var6 + 3];
         byte var9;
         if (var4[var6] > 0) {
            var9 = 1;
         } else {
            var9 = -1;
         }

         double var10 = (double)var4[var7] / (double)var4[var6];
         double var12 = (double)var4[var8] / (double)var4[var6];
         int[] var14 = new int[]{0, 0, 0};
         int var15 = 0;

         for(int var16 = var4[var6] + var9; var15 != var16; var15 += var9) {
            var14[var6] = Mth.floor((double)(var1[var6] + var15) + (double)0.5F);
            var14[var7] = Mth.floor((double)var1[var7] + (double)var15 * var10 + (double)0.5F);
            var14[var8] = Mth.floor((double)var1[var8] + (double)var15 * var12 + (double)0.5F);
            this.thisLevel.setTileNoUpdate(var14[0], var14[1], var14[2], var3);
         }

      }
   }

   void makeFoliage() {
      int var1 = 0;

      for(int var2 = this.foliageCoords.length; var1 < var2; ++var1) {
         int var3 = this.foliageCoords[var1][0];
         int var4 = this.foliageCoords[var1][1];
         int var5 = this.foliageCoords[var1][2];
         this.foliageCluster(var3, var4, var5);
      }

   }

   boolean trimBranches(int var1) {
      return !((double)var1 < (double)this.height * 0.2);
   }

   void makeTrunk() {
      int var1 = this.origin[0];
      int var2 = this.origin[1];
      int var3 = this.origin[1] + this.trunkHeight;
      int var4 = this.origin[2];
      int[] var5 = new int[]{var1, var2, var4};
      int[] var6 = new int[]{var1, var3, var4};
      this.limb(var5, var6, 17);
      if (this.trunkWidth == 2) {
         int var10002 = var5[0]++;
         var10002 = var6[0]++;
         this.limb(var5, var6, 17);
         var10002 = var5[2]++;
         var10002 = var6[2]++;
         this.limb(var5, var6, 17);
         var5[0] += -1;
         var6[0] += -1;
         this.limb(var5, var6, 17);
      }

   }

   void makeBranches() {
      int var1 = 0;
      int var2 = this.foliageCoords.length;

      for(int[] var3 = new int[]{this.origin[0], this.origin[1], this.origin[2]}; var1 < var2; ++var1) {
         int[] var4 = this.foliageCoords[var1];
         int[] var5 = new int[]{var4[0], var4[1], var4[2]};
         var3[1] = var4[3];
         int var6 = var3[1] - this.origin[1];
         if (this.trimBranches(var6)) {
            this.limb(var3, var5, 17);
         }
      }

   }

   int checkLine(int[] var1, int[] var2) {
      int[] var3 = new int[]{0, 0, 0};
      byte var4 = 0;

      byte var5;
      for(var5 = 0; var4 < 3; ++var4) {
         var3[var4] = var2[var4] - var1[var4];
         if (Math.abs(var3[var4]) > Math.abs(var3[var5])) {
            var5 = var4;
         }
      }

      if (var3[var5] == 0) {
         return -1;
      } else {
         byte var6 = axisConversionArray[var5];
         byte var7 = axisConversionArray[var5 + 3];
         byte var8;
         if (var3[var5] > 0) {
            var8 = 1;
         } else {
            var8 = -1;
         }

         double var9 = (double)var3[var6] / (double)var3[var5];
         double var11 = (double)var3[var7] / (double)var3[var5];
         int[] var13 = new int[]{0, 0, 0};
         int var14 = 0;

         int var15;
         for(var15 = var3[var5] + var8; var14 != var15; var14 += var8) {
            var13[var5] = var1[var5] + var14;
            var13[var6] = (int)((double)var1[var6] + (double)var14 * var9);
            var13[var7] = (int)((double)var1[var7] + (double)var14 * var11);
            int var16 = this.thisLevel.getTile(var13[0], var13[1], var13[2]);
            if (var16 != 0 && var16 != 18) {
               break;
            }
         }

         return var14 == var15 ? -1 : Math.abs(var14);
      }
   }

   boolean checkLocation() {
      int[] var1 = new int[]{this.origin[0], this.origin[1], this.origin[2]};
      int[] var2 = new int[]{this.origin[0], this.origin[1] + this.height - 1, this.origin[2]};
      int var3 = this.thisLevel.getTile(this.origin[0], this.origin[1] - 1, this.origin[2]);
      if (var3 != 2 && var3 != 3) {
         return false;
      } else {
         int var4 = this.checkLine(var1, var2);
         if (var4 == -1) {
            return true;
         } else if (var4 < 6) {
            return false;
         } else {
            this.height = var4;
            return true;
         }
      }
   }

   public void init(double var1, double var3, double var5) {
      this.heightVariance = (int)(var1 * (double)12.0F);
      if (var1 > (double)0.5F) {
         this.foliageHeight = 5;
      }

      this.widthScale = var3;
      this.foliageDensity = var5;
   }

   public boolean place(Level var1, Random var2, int var3, int var4, int var5) {
      this.thisLevel = var1;
      long var6 = var2.nextLong();
      this.rnd.setSeed(var6);
      this.origin[0] = var3;
      this.origin[1] = var4;
      this.origin[2] = var5;
      if (this.height == 0) {
         this.height = 5 + this.rnd.nextInt(this.heightVariance);
      }

      if (!this.checkLocation()) {
         return false;
      } else {
         this.prepare();
         this.makeFoliage();
         this.makeTrunk();
         this.makeBranches();
         return true;
      }
   }
}
