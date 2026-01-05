package net.minecraft.world.level;

import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.entity.TileEntity;

public class Region implements LevelSource {
   private int xc1;
   private int zc1;
   private LevelChunk[][] chunks;
   private Level level;

   public Region(Level var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      this.level = var1;
      this.xc1 = var2 >> 4;
      this.zc1 = var4 >> 4;
      int var8 = var5 >> 4;
      int var9 = var7 >> 4;
      this.chunks = new LevelChunk[var8 - this.xc1 + 1][var9 - this.zc1 + 1];

      for(int var10 = this.xc1; var10 <= var8; ++var10) {
         for(int var11 = this.zc1; var11 <= var9; ++var11) {
            this.chunks[var10 - this.xc1][var11 - this.zc1] = var1.getChunk(var10, var11);
         }
      }

   }

   public int getTile(int var1, int var2, int var3) {
      if (var2 < 0) {
         return 0;
      } else if (var2 >= 128) {
         return 0;
      } else {
         int var4 = (var1 >> 4) - this.xc1;
         int var5 = (var3 >> 4) - this.zc1;

         try {
            LevelChunk var6 = this.chunks[var4][var5];
            return var6 == null ? 0 : var6.getTile(var1 & 15, var2, var3 & 15);
         } catch (ArrayIndexOutOfBoundsException var7) {
            return 0;
         }
      }
   }

   public TileEntity getTileEntity(int var1, int var2, int var3) {
      int var4 = (var1 >> 4) - this.xc1;
      int var5 = (var3 >> 4) - this.zc1;
      return this.chunks[var4][var5].getTileEntity(var1 & 15, var2, var3 & 15);
   }

   public float getBrightness(int var1, int var2, int var3) {
      return this.level.dimension.brightnessRamp[this.getRawBrightness(var1, var2, var3)];
   }

   public int getRawBrightness(int var1, int var2, int var3) {
      return this.getRawBrightness(var1, var2, var3, true);
   }

   public int getRawBrightness(int var1, int var2, int var3, boolean var4) {
      if (var1 >= -32000000 && var3 >= -32000000 && var1 < 32000000 && var3 <= 32000000) {
         if (var4) {
            int var5 = this.getTile(var1, var2, var3);
            if (var5 == Tile.stoneSlabHalf.id || var5 == Tile.farmland.id) {
               int var13 = this.getRawBrightness(var1, var2 + 1, var3, false);
               int var7 = this.getRawBrightness(var1 + 1, var2, var3, false);
               int var8 = this.getRawBrightness(var1 - 1, var2, var3, false);
               int var9 = this.getRawBrightness(var1, var2, var3 + 1, false);
               int var10 = this.getRawBrightness(var1, var2, var3 - 1, false);
               if (var7 > var13) {
                  var13 = var7;
               }

               if (var8 > var13) {
                  var13 = var8;
               }

               if (var9 > var13) {
                  var13 = var9;
               }

               if (var10 > var13) {
                  var13 = var10;
               }

               return var13;
            }
         }

         if (var2 < 0) {
            return 0;
         } else if (var2 >= 128) {
            int var12 = 15 - this.level.skyDarken;
            if (var12 < 0) {
               var12 = 0;
            }

            return var12;
         } else {
            int var11 = (var1 >> 4) - this.xc1;
            int var6 = (var3 >> 4) - this.zc1;
            return this.chunks[var11][var6].getRawBrightness(var1 & 15, var2, var3 & 15, this.level.skyDarken);
         }
      } else {
         return 15;
      }
   }

   public int getData(int var1, int var2, int var3) {
      if (var2 < 0) {
         return 0;
      } else if (var2 >= 128) {
         return 0;
      } else {
         int var4 = (var1 >> 4) - this.xc1;
         int var5 = (var3 >> 4) - this.zc1;
         return this.chunks[var4][var5].getData(var1 & 15, var2, var3 & 15);
      }
   }

   public Material getMaterial(int var1, int var2, int var3) {
      int var4 = this.getTile(var1, var2, var3);
      return var4 == 0 ? Material.air : Tile.tiles[var4].material;
   }

   public boolean isSolidTile(int var1, int var2, int var3) {
      Tile var4 = Tile.tiles[this.getTile(var1, var2, var3)];
      return var4 == null ? false : var4.isSolidRender();
   }

   public BiomeSource getBiomeSource() {
      return this.level.getBiomeSource();
   }
}
