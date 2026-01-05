package net.minecraft.world.level.levelgen.feature;

import java.util.Random;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;

public class HellFireFeature extends Feature {
   public HellFireFeature() {
   }

   public boolean place(Level var1, Random var2, int var3, int var4, int var5) {
      for(int var6 = 0; var6 < 64; ++var6) {
         int var7 = var3 + var2.nextInt(8) - var2.nextInt(8);
         int var8 = var4 + var2.nextInt(4) - var2.nextInt(4);
         int var9 = var5 + var2.nextInt(8) - var2.nextInt(8);
         if (var1.isEmptyTile(var7, var8, var9) && var1.getTile(var7, var8 - 1, var9) == Tile.hellRock.id) {
            var1.setTile(var7, var8, var9, Tile.fire.id);
         }
      }

      return true;
   }
}
