package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;

public class GrassTile extends Tile {
   public static final int MIN_BRIGHTNESS = 4;

   protected GrassTile(int var1) {
      super(var1, Material.dirt);
      this.tex = 3;
      this.setTicking(true);
   }

   public int getTexture(LevelSource var1, int var2, int var3, int var4, int var5) {
      if (var5 == 1) {
         return 0;
      } else if (var5 == 0) {
         return 2;
      } else {
         Material var6 = var1.getMaterial(var2, var3 + 1, var4);
         return var6 != Material.topSnow && var6 != Material.snow ? 3 : 68;
      }
   }

   public int getColor(LevelSource var1, int var2, int var3, int var4) {
      var1.getBiomeSource().getBiomeBlock(var2, var4, 1, 1);
      double var5 = var1.getBiomeSource().temperatures[0];
      double var7 = var1.getBiomeSource().downfalls[0];
      return GrassColor.get(var5, var7);
   }

   public void tick(Level var1, int var2, int var3, int var4, Random var5) {
      if (!var1.isOnline) {
         if (var1.getRawBrightness(var2, var3 + 1, var4) < 4 && var1.getMaterial(var2, var3 + 1, var4).blocksLight()) {
            if (var5.nextInt(4) != 0) {
               return;
            }

            var1.setTile(var2, var3, var4, Tile.dirt.id);
         } else if (var1.getRawBrightness(var2, var3 + 1, var4) >= 9) {
            int var6 = var2 + var5.nextInt(3) - 1;
            int var7 = var3 + var5.nextInt(5) - 3;
            int var8 = var4 + var5.nextInt(3) - 1;
            if (var1.getTile(var6, var7, var8) == Tile.dirt.id && var1.getRawBrightness(var6, var7 + 1, var8) >= 4 && !var1.getMaterial(var6, var7 + 1, var8).blocksLight()) {
               var1.setTile(var6, var7, var8, Tile.grass.id);
            }
         }

      }
   }

   public int getResource(int var1, Random var2) {
      return Tile.dirt.getResource(0, var2);
   }
}
