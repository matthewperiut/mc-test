package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.entity.item.FallingTile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;

public class SandTile extends Tile {
   public static boolean instaFall = false;

   public SandTile(int var1, int var2) {
      super(var1, var2, Material.sand);
   }

   public void onPlace(Level var1, int var2, int var3, int var4) {
      var1.addToTickNextTick(var2, var3, var4, this.id);
   }

   public void neighborChanged(Level var1, int var2, int var3, int var4, int var5) {
      var1.addToTickNextTick(var2, var3, var4, this.id);
   }

   public void tick(Level var1, int var2, int var3, int var4, Random var5) {
      this.checkSlide(var1, var2, var3, var4);
   }

   private void checkSlide(Level var1, int var2, int var3, int var4) {
      if (isFree(var1, var2, var3 - 1, var4) && var3 >= 0) {
         byte var8 = 32;
         if (!instaFall && var1.hasChunksAt(var2 - var8, var3 - var8, var4 - var8, var2 + var8, var3 + var8, var4 + var8)) {
            FallingTile var9 = new FallingTile(var1, (double)((float)var2 + 0.5F), (double)((float)var3 + 0.5F), (double)((float)var4 + 0.5F), this.id);
            var1.addEntity(var9);
         } else {
            var1.setTile(var2, var3, var4, 0);

            while(isFree(var1, var2, var3 - 1, var4) && var3 > 0) {
               --var3;
            }

            if (var3 > 0) {
               var1.setTile(var2, var3, var4, this.id);
            }
         }
      }

   }

   public int getTickDelay() {
      return 3;
   }

   public static boolean isFree(Level var0, int var1, int var2, int var3) {
      int var4 = var0.getTile(var1, var2, var3);
      if (var4 == 0) {
         return true;
      } else if (var4 == Tile.fire.id) {
         return true;
      } else {
         Material var5 = Tile.tiles[var4].material;
         if (var5 == Material.water) {
            return true;
         } else {
            return var5 == Material.lava;
         }
      }
   }
}
