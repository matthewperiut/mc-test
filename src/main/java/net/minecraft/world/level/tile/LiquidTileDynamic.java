package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;

public class LiquidTileDynamic extends LiquidTile {
   int maxCount = 0;
   boolean[] result = new boolean[4];
   int[] dist = new int[4];

   protected LiquidTileDynamic(int var1, Material var2) {
      super(var1, var2);
   }

   private void setStatic(Level var1, int var2, int var3, int var4) {
      int var5 = var1.getData(var2, var3, var4);
      var1.setTileAndDataNoUpdate(var2, var3, var4, this.id + 1, var5);
      var1.setTilesDirty(var2, var3, var4, var2, var3, var4);
      var1.sendTileUpdated(var2, var3, var4);
   }

   public void tick(Level var1, int var2, int var3, int var4, Random var5) {
      int var6 = this.getDepth(var1, var2, var3, var4);
      byte var7 = 1;
      if (this.material == Material.lava && !var1.dimension.ultraWarm) {
         var7 = 2;
      }

      boolean var8 = true;
      if (var6 > 0) {
         int var9 = -100;
         this.maxCount = 0;
         var9 = this.getHighest(var1, var2 - 1, var3, var4, var9);
         var9 = this.getHighest(var1, var2 + 1, var3, var4, var9);
         var9 = this.getHighest(var1, var2, var3, var4 - 1, var9);
         var9 = this.getHighest(var1, var2, var3, var4 + 1, var9);
         int var10 = var9 + var7;
         if (var10 >= 8 || var9 < 0) {
            var10 = -1;
         }

         if (this.getDepth(var1, var2, var3 + 1, var4) >= 0) {
            int var11 = this.getDepth(var1, var2, var3 + 1, var4);
            if (var11 >= 8) {
               var10 = var11;
            } else {
               var10 = var11 + 8;
            }
         }

         if (this.maxCount >= 2 && this.material == Material.water) {
            if (var1.isSolidTile(var2, var3 - 1, var4)) {
               var10 = 0;
            } else if (var1.getMaterial(var2, var3 - 1, var4) == this.material && var1.getData(var2, var3, var4) == 0) {
               var10 = 0;
            }
         }

         if (this.material == Material.lava && var6 < 8 && var10 < 8 && var10 > var6 && var5.nextInt(4) != 0) {
            var10 = var6;
            var8 = false;
         }

         if (var10 != var6) {
            var6 = var10;
            if (var10 < 0) {
               var1.setTile(var2, var3, var4, 0);
            } else {
               var1.setData(var2, var3, var4, var10);
               var1.addToTickNextTick(var2, var3, var4, this.id);
               var1.updateNeighborsAt(var2, var3, var4, this.id);
            }
         } else if (var8) {
            this.setStatic(var1, var2, var3, var4);
         }
      } else {
         this.setStatic(var1, var2, var3, var4);
      }

      if (this.canSpreadTo(var1, var2, var3 - 1, var4)) {
         if (var6 >= 8) {
            var1.setTileAndData(var2, var3 - 1, var4, this.id, var6);
         } else {
            var1.setTileAndData(var2, var3 - 1, var4, this.id, var6 + 8);
         }
      } else if (var6 >= 0 && (var6 == 0 || this.isWaterBlocking(var1, var2, var3 - 1, var4))) {
         boolean[] var16 = this.getSpread(var1, var2, var3, var4);
         int var17 = var6 + var7;
         if (var6 >= 8) {
            var17 = 1;
         }

         if (var17 >= 8) {
            return;
         }

         if (var16[0]) {
            this.trySpreadTo(var1, var2 - 1, var3, var4, var17);
         }

         if (var16[1]) {
            this.trySpreadTo(var1, var2 + 1, var3, var4, var17);
         }

         if (var16[2]) {
            this.trySpreadTo(var1, var2, var3, var4 - 1, var17);
         }

         if (var16[3]) {
            this.trySpreadTo(var1, var2, var3, var4 + 1, var17);
         }
      }

   }

   private void trySpreadTo(Level var1, int var2, int var3, int var4, int var5) {
      if (this.canSpreadTo(var1, var2, var3, var4)) {
         int var6 = var1.getTile(var2, var3, var4);
         if (var6 > 0) {
            if (this.material == Material.lava) {
               this.fizz(var1, var2, var3, var4);
            } else {
               Tile.tiles[var6].spawnResources(var1, var2, var3, var4, var1.getData(var2, var3, var4));
            }
         }

         var1.setTileAndData(var2, var3, var4, this.id, var5);
      }

   }

   private int getSlopeDistance(Level var1, int var2, int var3, int var4, int var5, int var6) {
      int var7 = 1000;

      for(int var8 = 0; var8 < 4; ++var8) {
         if ((var8 != 0 || var6 != 1) && (var8 != 1 || var6 != 0) && (var8 != 2 || var6 != 3) && (var8 != 3 || var6 != 2)) {
            int var9 = var2;
            int var11 = var4;
            if (var8 == 0) {
               var9 = var2 - 1;
            }

            if (var8 == 1) {
               ++var9;
            }

            if (var8 == 2) {
               var11 = var4 - 1;
            }

            if (var8 == 3) {
               ++var11;
            }

            if (!this.isWaterBlocking(var1, var9, var3, var11) && (var1.getMaterial(var9, var3, var11) != this.material || var1.getData(var9, var3, var11) != 0)) {
               if (!this.isWaterBlocking(var1, var9, var3 - 1, var11)) {
                  return var5;
               }

               if (var5 < 4) {
                  int var12 = this.getSlopeDistance(var1, var9, var3, var11, var5 + 1, var8);
                  if (var12 < var7) {
                     var7 = var12;
                  }
               }
            }
         }
      }

      return var7;
   }

   private boolean[] getSpread(Level var1, int var2, int var3, int var4) {
      for(int var5 = 0; var5 < 4; ++var5) {
         this.dist[var5] = 1000;
         int var6 = var2;
         int var8 = var4;
         if (var5 == 0) {
            var6 = var2 - 1;
         }

         if (var5 == 1) {
            ++var6;
         }

         if (var5 == 2) {
            var8 = var4 - 1;
         }

         if (var5 == 3) {
            ++var8;
         }

         if (!this.isWaterBlocking(var1, var6, var3, var8) && (var1.getMaterial(var6, var3, var8) != this.material || var1.getData(var6, var3, var8) != 0)) {
            if (!this.isWaterBlocking(var1, var6, var3 - 1, var8)) {
               this.dist[var5] = 0;
            } else {
               this.dist[var5] = this.getSlopeDistance(var1, var6, var3, var8, 1, var5);
            }
         }
      }

      int var9 = this.dist[0];

      for(int var10 = 1; var10 < 4; ++var10) {
         if (this.dist[var10] < var9) {
            var9 = this.dist[var10];
         }
      }

      for(int var11 = 0; var11 < 4; ++var11) {
         this.result[var11] = this.dist[var11] == var9;
      }

      return this.result;
   }

   private boolean isWaterBlocking(Level var1, int var2, int var3, int var4) {
      int var5 = var1.getTile(var2, var3, var4);
      if (var5 != Tile.door_wood.id && var5 != Tile.door_iron.id && var5 != Tile.sign.id && var5 != Tile.ladder.id && var5 != Tile.reeds.id) {
         if (var5 == 0) {
            return false;
         } else {
            Material var6 = Tile.tiles[var5].material;
            return var6.isSolid();
         }
      } else {
         return true;
      }
   }

   protected int getHighest(Level var1, int var2, int var3, int var4, int var5) {
      int var6 = this.getDepth(var1, var2, var3, var4);
      if (var6 < 0) {
         return var5;
      } else {
         if (var6 == 0) {
            ++this.maxCount;
         }

         if (var6 >= 8) {
            var6 = 0;
         }

         return var5 >= 0 && var6 >= var5 ? var5 : var6;
      }
   }

   private boolean canSpreadTo(Level var1, int var2, int var3, int var4) {
      Material var5 = var1.getMaterial(var2, var3, var4);
      if (var5 == this.material) {
         return false;
      } else if (var5 == Material.lava) {
         return false;
      } else {
         return !this.isWaterBlocking(var1, var2, var3, var4);
      }
   }

   public void onPlace(Level var1, int var2, int var3, int var4) {
      super.onPlace(var1, var2, var3, var4);
      if (var1.getTile(var2, var3, var4) == this.id) {
         var1.addToTickNextTick(var2, var3, var4, this.id);
      }

   }
}
