package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;

public class TreeTile extends Tile {
   public static final int DARK_TRUNK = 1;
   public static final int BIRCH_TRUNK = 2;

   protected TreeTile(int var1) {
      super(var1, Material.wood);
      this.tex = 20;
   }

   public int getResourceCount(Random var1) {
      return 1;
   }

   public int getResource(int var1, Random var2) {
      return Tile.treeTrunk.id;
   }

   public void onRemove(Level var1, int var2, int var3, int var4) {
      byte var5 = 4;
      int var6 = var5 + 1;
      if (var1.hasChunksAt(var2 - var6, var3 - var6, var4 - var6, var2 + var6, var3 + var6, var4 + var6)) {
         for(int var7 = -var5; var7 <= var5; ++var7) {
            for(int var8 = -var5; var8 <= var5; ++var8) {
               for(int var9 = -var5; var9 <= var5; ++var9) {
                  int var10 = var1.getTile(var2 + var7, var3 + var8, var4 + var9);
                  if (var10 == Tile.leaves.id) {
                     int var11 = var1.getData(var2 + var7, var3 + var8, var4 + var9);
                     if ((var11 & 4) == 0) {
                        var1.setDataNoUpdate(var2 + var7, var3 + var8, var4 + var9, var11 | 4);
                     }
                  }
               }
            }
         }
      }

   }

   public int getTexture(int var1, int var2) {
      if (var1 == 1) {
         return 21;
      } else if (var1 == 0) {
         return 21;
      } else if (var2 == 1) {
         return 116;
      } else {
         return var2 == 2 ? 117 : 20;
      }
   }

   protected int getSpawnResourcesAuxValue(int var1) {
      return var1;
   }
}
