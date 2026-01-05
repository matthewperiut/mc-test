package net.minecraft.world.level.levelgen.feature;

import java.util.Random;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;

public class HouseFeature extends Feature {
   public HouseFeature() {
   }

   public boolean place(Level var1, Random var2, int var3, int var4, int var5) {
      int var6 = var2.nextInt(4) + 5;
      int var7 = var2.nextInt(2) + 3;
      int var8 = var2.nextInt(4) + 5;
      int var9 = var3 - var6 / 2;
      int var10 = var4;
      int var11 = var5 - var8 / 2;

      for(int var12 = var9; var12 < var9 + var6; ++var12) {
         for(int var13 = var11; var13 < var11 + var8; ++var13) {
            if (!var1.getMaterial(var12, var4 - 1, var13).blocksMotion()) {
               return false;
            }

            if ((var13 == var11 + 1 || var13 == var11 + var8 - 2) && var1.getMaterial(var12, var4, var13).blocksMotion()) {
               return false;
            }
         }
      }

      ++var9;
      ++var11;
      var6 -= 2;
      var8 -= 2;

      for(int var19 = var9; var19 < var9 + var6; ++var19) {
         for(int var20 = var10 - 1; var20 < var10 + var7; ++var20) {
            for(int var14 = var11; var14 < var11 + var8; ++var14) {
               if (var20 == var10 - 1 || var20 == var10 + var7 - 1 || var19 == var9 || var14 == var11 || var19 == var9 + var6 - 1 || var14 == var11 + var8 - 1) {
                  var1.setTile(var19, var20, var14, Tile.redBrick.id);
               }
            }
         }
      }

      return true;
   }
}
