package net.minecraft.world.level.levelgen.feature;

import java.util.Random;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Bush;
import net.minecraft.world.level.tile.Tile;

public class FlowerFeature extends Feature {
   private int tile;

   public FlowerFeature(int var1) {
      this.tile = var1;
   }

   public boolean place(Level var1, Random var2, int var3, int var4, int var5) {
      for(int var6 = 0; var6 < 64; ++var6) {
         int var7 = var3 + var2.nextInt(8) - var2.nextInt(8);
         int var8 = var4 + var2.nextInt(4) - var2.nextInt(4);
         int var9 = var5 + var2.nextInt(8) - var2.nextInt(8);
         if (var1.isEmptyTile(var7, var8, var9) && ((Bush)Tile.tiles[this.tile]).canSurvive(var1, var7, var8, var9)) {
            var1.setTileNoUpdate(var7, var8, var9, this.tile);
         }
      }

      return true;
   }
}
