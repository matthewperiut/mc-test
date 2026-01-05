package net.minecraft.world.level.tile;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;

public class Sponge extends Tile {
   public static final int RANGE = 2;

   protected Sponge(int var1) {
      super(var1, Material.sponge);
      this.tex = 48;
   }

   public void onPlace(Level var1, int var2, int var3, int var4) {
      byte var5 = 2;

      for(int var6 = var2 - var5; var6 <= var2 + var5; ++var6) {
         for(int var7 = var3 - var5; var7 <= var3 + var5; ++var7) {
            for(int var8 = var4 - var5; var8 <= var4 + var5; ++var8) {
               if (var1.getMaterial(var6, var7, var8) == Material.water) {
               }
            }
         }
      }

   }

   public void onRemove(Level var1, int var2, int var3, int var4) {
      byte var5 = 2;

      for(int var6 = var2 - var5; var6 <= var2 + var5; ++var6) {
         for(int var7 = var3 - var5; var7 <= var3 + var5; ++var7) {
            for(int var8 = var4 - var5; var8 <= var4 + var5; ++var8) {
               var1.updateNeighborsAt(var6, var7, var8, var1.getTile(var6, var7, var8));
            }
         }
      }

   }
}
