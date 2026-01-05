package net.minecraft.world.level.tile;

import net.minecraft.world.level.material.Material;

public class ClothTile extends Tile {
   private static final int COLOR_TEX_POS = 113;

   public ClothTile() {
      super(35, 64, Material.cloth);
   }

   public int getTexture(int var1, int var2) {
      if (var2 == 0) {
         return this.tex;
      } else {
         var2 = ~(var2 & 15);
         return 113 + ((var2 & 8) >> 3) + (var2 & 7) * 16;
      }
   }

   protected int getSpawnResourcesAuxValue(int var1) {
      return var1;
   }

   public static int getTileDataForItemAuxValue(int var0) {
      return ~var0 & 15;
   }

   public static int getItemAuxValueForTileData(int var0) {
      return ~var0 & 15;
   }
}
