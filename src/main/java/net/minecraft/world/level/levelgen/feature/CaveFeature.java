package net.minecraft.world.level.levelgen.feature;

import java.util.ArrayList;
import java.util.Random;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.TilePos;
import net.minecraft.world.level.tile.Tile;
import util.Mth;

public class CaveFeature extends Feature {
   public CaveFeature() {
   }

   public boolean place(Level var1, Random var2, int var3, int var4, int var5) {
      float var6 = var2.nextFloat() * (float)Math.PI;
      double var7 = (double)8.0F;
      double var9 = (double)(var3 + 8) + (double)Mth.sin(var6) * var7;
      double var11 = (double)(var3 + 8) - (double)Mth.sin(var6) * var7;
      double var13 = (double)(var5 + 8) + (double)Mth.cos(var6) * var7;
      double var15 = (double)(var5 + 8) - (double)Mth.cos(var6) * var7;
      double var17 = (double)(var4 + var2.nextInt(8) + 2);
      double var19 = (double)(var4 + var2.nextInt(8) + 2);
      double var21 = var2.nextDouble() * (double)4.0F + (double)2.0F;
      double var23 = var2.nextDouble() * 0.6;
      long var25 = var2.nextLong();
      var2.setSeed(var25);
      ArrayList var27 = new ArrayList();

      for(int var28 = 0; var28 <= 16; ++var28) {
         double var29 = var9 + (var11 - var9) * (double)var28 / (double)16.0F;
         double var31 = var17 + (var19 - var17) * (double)var28 / (double)16.0F;
         double var33 = var13 + (var15 - var13) * (double)var28 / (double)16.0F;
         double var35 = var2.nextDouble();
         double var37 = ((double)Mth.sin((float)var28 / 16.0F * (float)Math.PI) * var21 + (double)1.0F) * var35 + (double)1.0F;
         double var39 = ((double)Mth.sin((float)var28 / 16.0F * (float)Math.PI) * var21 + (double)1.0F) * var35 + (double)1.0F;

         for(int var41 = (int)(var29 - var37 / (double)2.0F); var41 <= (int)(var29 + var37 / (double)2.0F); ++var41) {
            for(int var42 = (int)(var31 - var39 / (double)2.0F); var42 <= (int)(var31 + var39 / (double)2.0F); ++var42) {
               for(int var43 = (int)(var33 - var37 / (double)2.0F); var43 <= (int)(var33 + var37 / (double)2.0F); ++var43) {
                  double var44 = ((double)var41 + (double)0.5F - var29) / (var37 / (double)2.0F);
                  double var46 = ((double)var42 + (double)0.5F - var31) / (var39 / (double)2.0F);
                  double var48 = ((double)var43 + (double)0.5F - var33) / (var37 / (double)2.0F);
                  if (var44 * var44 + var46 * var46 + var48 * var48 < var2.nextDouble() * var23 + ((double)1.0F - var23) && !var1.isEmptyTile(var41, var42, var43)) {
                     for(int var50 = var41 - 2; var50 <= var41 + 1; ++var50) {
                        for(int var51 = var42 - 1; var51 <= var42 + 1; ++var51) {
                           for(int var52 = var43 - 1; var52 <= var43 + 1; ++var52) {
                              if (var50 <= var3 || var52 <= var5 || var50 >= var3 + 16 - 1 || var52 >= var5 + 16 - 1) {
                                 return false;
                              }

                              if (var1.getMaterial(var50, var51, var52).isLiquid()) {
                                 return false;
                              }
                           }
                        }
                     }

                     var27.add(new TilePos(var41, var42, var43));
                  }
               }
            }
         }
      }

      for(int var53 = 0; var53 < var27.size(); ++var53) {
         TilePos var55 = (TilePos)var27.get(var53);
         var1.setTileNoUpdate(var55.x, var55.y, var55.z, 0);
      }

      for(int var54 = 0; var54 < var27.size(); ++var54) {
         TilePos var56 = (TilePos)var27.get(var54);
         if (var1.getTile(var56.x, var56.y - 1, var56.z) == Tile.dirt.id && var1.getRawBrightness(var56.x, var56.y, var56.z) > 8) {
            var1.setTileNoUpdate(var56.x, var56.y - 1, var56.z, Tile.grass.id);
         }
      }

      return true;
   }
}
