package net.minecraft.world.level.chunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.TilePos;
import net.minecraft.world.level.tile.EntityTile;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;
import util.Mth;

public class LevelChunk {
   public static boolean touchedSky;
   public byte[] blocks;
   public boolean loaded;
   public Level level;
   public DataLayer data;
   public DataLayer skyLight;
   public DataLayer blockLight;
   public byte[] heightmap;
   public int minHeight;
   public final int x;
   public final int z;
   public Map<TilePos, TileEntity> tileEntities;
   public List<Entity>[] entityBlocks;
   public boolean terrainPopulated;
   public boolean unsaved;
   public boolean dontSave;
   public boolean lastSaveHadEntities;
   public long lastSaveTime;

   public LevelChunk(Level var1, int var2, int var3) {
      this.tileEntities = new HashMap();
      this.entityBlocks = new List[8];
      this.terrainPopulated = false;
      this.unsaved = false;
      this.lastSaveHadEntities = false;
      this.lastSaveTime = 0L;
      this.level = var1;
      this.x = var2;
      this.z = var3;
      this.heightmap = new byte[256];

      for(int var4 = 0; var4 < this.entityBlocks.length; ++var4) {
         this.entityBlocks[var4] = new ArrayList();
      }

   }

   public LevelChunk(Level var1, byte[] var2, int var3, int var4) {
      this(var1, var3, var4);
      this.blocks = var2;
      this.data = new DataLayer(var2.length);
      this.skyLight = new DataLayer(var2.length);
      this.blockLight = new DataLayer(var2.length);
   }

   public boolean isAt(int var1, int var2) {
      return var1 == this.x && var2 == this.z;
   }

   public int getHeightmap(int var1, int var2) {
      return this.heightmap[var2 << 4 | var1] & 255;
   }

   public void recalcBlockLights() {
   }

   public void recalcHeightmapOnly() {
      int var1 = 127;

      for(int var2 = 0; var2 < 16; ++var2) {
         for(int var3 = 0; var3 < 16; ++var3) {
            int var4 = 127;

            for(int var5 = var2 << 11 | var3 << 7; var4 > 0 && Tile.lightBlock[this.blocks[var5 + var4 - 1]] == 0; --var4) {
            }

            this.heightmap[var3 << 4 | var2] = (byte)var4;
            if (var4 < var1) {
               var1 = var4;
            }
         }
      }

      this.minHeight = var1;
      this.unsaved = true;
   }

   public void recalcHeightmap() {
      int var1 = 127;

      for(int var2 = 0; var2 < 16; ++var2) {
         for(int var3 = 0; var3 < 16; ++var3) {
            int var4 = 127;

            int var5;
            for(var5 = var2 << 11 | var3 << 7; var4 > 0 && Tile.lightBlock[this.blocks[var5 + var4 - 1]] == 0; --var4) {
            }

            this.heightmap[var3 << 4 | var2] = (byte)var4;
            if (var4 < var1) {
               var1 = var4;
            }

            if (!this.level.dimension.hasCeiling) {
               int var6 = 15;
               int var7 = 127;

               while(true) {
                  var6 -= Tile.lightBlock[this.blocks[var5 + var7]];
                  if (var6 > 0) {
                     this.skyLight.set(var2, var7, var3, var6);
                  }

                  --var7;
                  if (var7 <= 0 || var6 <= 0) {
                     break;
                  }
               }
            }
         }
      }

      this.minHeight = var1;

      for(int var8 = 0; var8 < 16; ++var8) {
         for(int var9 = 0; var9 < 16; ++var9) {
            this.lightGaps(var8, var9);
         }
      }

      this.unsaved = true;
   }

   public void lightLava() {
      byte var1 = 32;

      for(int var2 = 0; var2 < 16; ++var2) {
         for(int var3 = 0; var3 < 16; ++var3) {
            int var4 = var2 << 11 | var3 << 7;

            for(int var5 = 0; var5 < 128; ++var5) {
               int var6 = Tile.lightEmission[this.blocks[var4 + var5]];
               if (var6 > 0) {
                  this.blockLight.set(var2, var5, var3, var6);
               }
            }

            int var10 = 15;

            for(int var11 = var1 - 2; var11 < 128 && var10 > 0; this.blockLight.set(var2, var11, var3, var10)) {
               ++var11;
               byte var7 = this.blocks[var4 + var11];
               int var8 = Tile.lightBlock[var7];
               int var9 = Tile.lightEmission[var7];
               if (var8 == 0) {
                  var8 = 1;
               }

               var10 -= var8;
               if (var9 > var10) {
                  var10 = var9;
               }

               if (var10 < 0) {
                  var10 = 0;
               }
            }
         }
      }

      this.level.updateLight(LightLayer.Block, this.x * 16, var1 - 1, this.z * 16, this.x * 16 + 16, var1 + 1, this.z * 16 + 16);
      this.unsaved = true;
   }

   private void lightGaps(int var1, int var2) {
      int var3 = this.getHeightmap(var1, var2);
      int var4 = this.x * 16 + var1;
      int var5 = this.z * 16 + var2;
      this.lightGap(var4 - 1, var5, var3);
      this.lightGap(var4 + 1, var5, var3);
      this.lightGap(var4, var5 - 1, var3);
      this.lightGap(var4, var5 + 1, var3);
   }

   private void lightGap(int var1, int var2, int var3) {
      int var4 = this.level.getHeightmap(var1, var2);
      if (var4 > var3) {
         this.level.updateLight(LightLayer.Sky, var1, var3, var2, var1, var4, var2);
         this.unsaved = true;
      } else if (var4 < var3) {
         this.level.updateLight(LightLayer.Sky, var1, var4, var2, var1, var3, var2);
         this.unsaved = true;
      }

   }

   private void recalcHeight(int var1, int var2, int var3) {
      int var4 = this.heightmap[var3 << 4 | var1] & 255;
      int var5 = var4;
      if (var2 > var4) {
         var5 = var2;
      }

      for(int var6 = var1 << 11 | var3 << 7; var5 > 0 && Tile.lightBlock[this.blocks[var6 + var5 - 1]] == 0; --var5) {
      }

      if (var5 != var4) {
         this.level.lightColumnChanged(var1, var3, var5, var4);
         this.heightmap[var3 << 4 | var1] = (byte)var5;
         if (var5 < this.minHeight) {
            this.minHeight = var5;
         } else {
            int var7 = 127;

            for(int var8 = 0; var8 < 16; ++var8) {
               for(int var9 = 0; var9 < 16; ++var9) {
                  if ((this.heightmap[var9 << 4 | var8] & 255) < var7) {
                     var7 = this.heightmap[var9 << 4 | var8] & 255;
                  }
               }
            }

            this.minHeight = var7;
         }

         int var12 = this.x * 16 + var1;
         int var13 = this.z * 16 + var3;
         if (var5 < var4) {
            for(int var14 = var5; var14 < var4; ++var14) {
               this.skyLight.set(var1, var14, var3, 15);
            }
         } else {
            this.level.updateLight(LightLayer.Sky, var12, var4, var13, var12, var5, var13);

            for(int var15 = var4; var15 < var5; ++var15) {
               this.skyLight.set(var1, var15, var3, 0);
            }
         }

         int var16 = 15;

         int var10;
         for(var10 = var5; var5 > 0 && var16 > 0; this.skyLight.set(var1, var5, var3, var16)) {
            --var5;
            int var11 = Tile.lightBlock[this.getTile(var1, var5, var3)];
            if (var11 == 0) {
               var11 = 1;
            }

            var16 -= var11;
            if (var16 < 0) {
               var16 = 0;
            }
         }

         while(var5 > 0 && Tile.lightBlock[this.getTile(var1, var5 - 1, var3)] == 0) {
            --var5;
         }

         if (var5 != var10) {
            this.level.updateLight(LightLayer.Sky, var12 - 1, var5, var13 - 1, var12 + 1, var10, var13 + 1);
         }

         this.unsaved = true;
      }
   }

   public int getTile(int var1, int var2, int var3) {
      return this.blocks[var1 << 11 | var3 << 7 | var2];
   }

   public boolean setTileAndData(int var1, int var2, int var3, int var4, int var5) {
      byte var6 = (byte)var4;
      int var7 = this.heightmap[var3 << 4 | var1] & 255;
      int var8 = this.blocks[var1 << 11 | var3 << 7 | var2] & 255;
      if (var8 == var4 && this.data.get(var1, var2, var3) == var5) {
         return false;
      } else {
         int var9 = this.x * 16 + var1;
         int var10 = this.z * 16 + var3;
         this.blocks[var1 << 11 | var3 << 7 | var2] = var6;
         if (var8 != 0 && !this.level.isOnline) {
            Tile.tiles[var8].onRemove(this.level, var9, var2, var10);
         }

         this.data.set(var1, var2, var3, var5);
         if (!this.level.dimension.hasCeiling) {
            if (Tile.lightBlock[var6] != 0) {
               if (var2 >= var7) {
                  this.recalcHeight(var1, var2 + 1, var3);
               }
            } else if (var2 == var7 - 1) {
               this.recalcHeight(var1, var2, var3);
            }

            this.level.updateLight(LightLayer.Sky, var9, var2, var10, var9, var2, var10);
         }

         this.level.updateLight(LightLayer.Block, var9, var2, var10, var9, var2, var10);
         this.lightGaps(var1, var3);
         this.data.set(var1, var2, var3, var5);
         if (var4 != 0) {
            Tile.tiles[var4].onPlace(this.level, var9, var2, var10);
         }

         this.unsaved = true;
         return true;
      }
   }

   public boolean setTile(int var1, int var2, int var3, int var4) {
      byte var5 = (byte)var4;
      int var6 = this.heightmap[var3 << 4 | var1] & 255;
      int var7 = this.blocks[var1 << 11 | var3 << 7 | var2] & 255;
      if (var7 == var4) {
         return false;
      } else {
         int var8 = this.x * 16 + var1;
         int var9 = this.z * 16 + var3;
         this.blocks[var1 << 11 | var3 << 7 | var2] = var5;
         if (var7 != 0) {
            Tile.tiles[var7].onRemove(this.level, var8, var2, var9);
         }

         this.data.set(var1, var2, var3, 0);
         if (Tile.lightBlock[var5] != 0) {
            if (var2 >= var6) {
               this.recalcHeight(var1, var2 + 1, var3);
            }
         } else if (var2 == var6 - 1) {
            this.recalcHeight(var1, var2, var3);
         }

         this.level.updateLight(LightLayer.Sky, var8, var2, var9, var8, var2, var9);
         this.level.updateLight(LightLayer.Block, var8, var2, var9, var8, var2, var9);
         this.lightGaps(var1, var3);
         if (var4 != 0 && !this.level.isOnline) {
            Tile.tiles[var4].onPlace(this.level, var8, var2, var9);
         }

         this.unsaved = true;
         return true;
      }
   }

   public int getData(int var1, int var2, int var3) {
      return this.data.get(var1, var2, var3);
   }

   public void setData(int var1, int var2, int var3, int var4) {
      this.unsaved = true;
      this.data.set(var1, var2, var3, var4);
   }

   public int getBrightness(LightLayer var1, int var2, int var3, int var4) {
      if (var1 == LightLayer.Sky) {
         return this.skyLight.get(var2, var3, var4);
      } else {
         return var1 == LightLayer.Block ? this.blockLight.get(var2, var3, var4) : 0;
      }
   }

   public void setBrightness(LightLayer var1, int var2, int var3, int var4, int var5) {
      this.unsaved = true;
      if (var1 == LightLayer.Sky) {
         this.skyLight.set(var2, var3, var4, var5);
      } else {
         if (var1 != LightLayer.Block) {
            return;
         }

         this.blockLight.set(var2, var3, var4, var5);
      }

   }

   public int getRawBrightness(int var1, int var2, int var3, int var4) {
      int var5 = this.skyLight.get(var1, var2, var3);
      if (var5 > 0) {
         touchedSky = true;
      }

      var5 -= var4;
      int var6 = this.blockLight.get(var1, var2, var3);
      if (var6 > var5) {
         var5 = var6;
      }

      return var5;
   }

   public void addEntity(Entity var1) {
      this.lastSaveHadEntities = true;
      int var2 = Mth.floor(var1.x / (double)16.0F);
      int var3 = Mth.floor(var1.z / (double)16.0F);
      if (var2 != this.x || var3 != this.z) {
         System.out.println("Wrong location! " + var1);
         Thread.dumpStack();
      }

      int var4 = Mth.floor(var1.y / (double)16.0F);
      if (var4 < 0) {
         var4 = 0;
      }

      if (var4 >= this.entityBlocks.length) {
         var4 = this.entityBlocks.length - 1;
      }

      var1.inChunk = true;
      var1.xChunk = this.x;
      var1.yChunk = var4;
      var1.zChunk = this.z;
      this.entityBlocks[var4].add(var1);
   }

   public void removeEntity(Entity var1) {
      this.removeEntity(var1, var1.yChunk);
   }

   public void removeEntity(Entity var1, int var2) {
      if (var2 < 0) {
         var2 = 0;
      }

      if (var2 >= this.entityBlocks.length) {
         var2 = this.entityBlocks.length - 1;
      }

      this.entityBlocks[var2].remove(var1);
   }

   public boolean isSkyLit(int var1, int var2, int var3) {
      return var2 >= (this.heightmap[var3 << 4 | var1] & 255);
   }

   public void skyBrightnessChanged() {
      int var1 = this.x * 16;
      int var2 = this.minHeight - 16;
      int var3 = this.z * 16;
      int var4 = this.x * 16 + 16;
      byte var5 = 127;
      int var6 = this.z * 16 + 16;
      this.level.setTilesDirty(var1, var2, var3, var4, var5, var6);
   }

   public TileEntity getTileEntity(int var1, int var2, int var3) {
      TilePos var4 = new TilePos(var1, var2, var3);
      TileEntity var5 = (TileEntity)this.tileEntities.get(var4);
      if (var5 == null) {
         int var6 = this.getTile(var1, var2, var3);
         if (!Tile.isEntityTile[var6]) {
            return null;
         }

         EntityTile var7 = (EntityTile)Tile.tiles[var6];
         var7.onPlace(this.level, this.x * 16 + var1, var2, this.z * 16 + var3);
         var5 = (TileEntity)this.tileEntities.get(var4);
      }

      return var5;
   }

   public void addTileEntity(TileEntity var1) {
      int var2 = var1.x - this.x * 16;
      int var3 = var1.y;
      int var4 = var1.z - this.z * 16;
      this.setTileEntity(var2, var3, var4, var1);
   }

   public void setTileEntity(int var1, int var2, int var3, TileEntity var4) {
      TilePos var5 = new TilePos(var1, var2, var3);
      var4.level = this.level;
      var4.x = this.x * 16 + var1;
      var4.y = var2;
      var4.z = this.z * 16 + var3;
      if (this.getTile(var1, var2, var3) != 0 && Tile.tiles[this.getTile(var1, var2, var3)] instanceof EntityTile) {
         if (this.loaded) {
            if (this.tileEntities.get(var5) != null) {
               this.level.tileEntityList.remove(this.tileEntities.get(var5));
            }

            this.level.tileEntityList.add(var4);
         }

         this.tileEntities.put(var5, var4);
      } else {
         System.out.println("Attempted to place a tile entity where there was no entity tile!");
      }
   }

   public void removeTileEntity(int var1, int var2, int var3) {
      TilePos var4 = new TilePos(var1, var2, var3);
      if (this.loaded) {
         this.level.tileEntityList.remove(this.tileEntities.remove(var4));
      }

   }

   public void load() {
      this.loaded = true;
      this.level.tileEntityList.addAll(this.tileEntities.values());

      for(int var1 = 0; var1 < this.entityBlocks.length; ++var1) {
         this.level.addEntities(this.entityBlocks[var1]);
      }

   }

   public void unload() {
      this.loaded = false;
      this.level.tileEntityList.removeAll(this.tileEntities.values());

      for(int var1 = 0; var1 < this.entityBlocks.length; ++var1) {
         this.level.removeEntities(this.entityBlocks[var1]);
      }

   }

   public void markUnsaved() {
      this.unsaved = true;
   }

   public void getEntities(Entity var1, AABB var2, List<Entity> var3) {
      int var4 = Mth.floor((var2.y0 - (double)2.0F) / (double)16.0F);
      int var5 = Mth.floor((var2.y1 + (double)2.0F) / (double)16.0F);
      if (var4 < 0) {
         var4 = 0;
      }

      if (var5 >= this.entityBlocks.length) {
         var5 = this.entityBlocks.length - 1;
      }

      for(int var6 = var4; var6 <= var5; ++var6) {
         List var7 = this.entityBlocks[var6];

         for(int var8 = 0; var8 < var7.size(); ++var8) {
            Entity var9 = (Entity)var7.get(var8);
            if (var9 != var1 && var9.bb.intersects(var2)) {
               var3.add(var9);
            }
         }
      }

   }

   public void getEntitiesOfClass(Class<? extends Entity> var1, AABB var2, List<Entity> var3) {
      int var4 = Mth.floor((var2.y0 - (double)2.0F) / (double)16.0F);
      int var5 = Mth.floor((var2.y1 + (double)2.0F) / (double)16.0F);
      if (var4 < 0) {
         var4 = 0;
      }

      if (var5 >= this.entityBlocks.length) {
         var5 = this.entityBlocks.length - 1;
      }

      for(int var6 = var4; var6 <= var5; ++var6) {
         List var7 = this.entityBlocks[var6];

         for(int var8 = 0; var8 < var7.size(); ++var8) {
            Entity var9 = (Entity)var7.get(var8);
            if (var1.isAssignableFrom(var9.getClass()) && var9.bb.intersects(var2)) {
               var3.add(var9);
            }
         }
      }

   }

   public int countEntities() {
      int var1 = 0;

      for(int var2 = 0; var2 < this.entityBlocks.length; ++var2) {
         var1 += this.entityBlocks[var2].size();
      }

      return var1;
   }

   public boolean shouldSave(boolean var1) {
      if (this.dontSave) {
         return false;
      } else {
         if (var1) {
            if (this.lastSaveHadEntities && this.level.time != this.lastSaveTime) {
               return true;
            }
         } else if (this.lastSaveHadEntities && this.level.time >= this.lastSaveTime + 600L) {
            return true;
         }

         return this.unsaved;
      }
   }

   public void setBlocks(byte[] var1, int var2) {
      for(int var3 = 0; var3 < 8192; ++var3) {
         this.blocks[var2 * 128 * 16 * 4 + var3] = var1[var3];
      }

      for(int var7 = var2 * 4; var7 < var2 * 4 + 4; ++var7) {
         for(int var4 = 0; var4 < 16; ++var4) {
            this.recalcHeight(var7, 0, var4);
         }
      }

      int var8 = this.x * 16;
      int var9 = this.z * 16;
      this.level.updateLight(LightLayer.Sky, var8 + var2 * 4, 0, var9, var8 + var2 * 4 + 4, 128, var9 + 16);
      this.level.updateLight(LightLayer.Block, var8 + var2 * 4, 0, var9, var8 + var2 * 4 + 4, 128, var9 + 16);

      for(int var5 = var2 * 4; var5 < var2 * 4 + 4; ++var5) {
         for(int var6 = 0; var6 < 16; ++var6) {
            this.lightGaps(var5, var6);
         }
      }

      this.level.setTilesDirty(var8 + var2 * 4, 0, var9, var8 + var2 * 4 + 4, 128, var9);
   }

   public int getBlocksAndData(byte[] var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8) {
      for(int var9 = var2; var9 < var5; ++var9) {
         for(int var10 = var4; var10 < var7; ++var10) {
            int var11 = var9 << 11 | var10 << 7 | var3;
            int var12 = var6 - var3;
            System.arraycopy(this.blocks, var11, var1, var8, var12);
            var8 += var12;
         }
      }

      for(int var13 = var2; var13 < var5; ++var13) {
         for(int var16 = var4; var16 < var7; ++var16) {
            int var19 = (var13 << 11 | var16 << 7 | var3) >> 1;
            int var22 = (var6 - var3) / 2;
            System.arraycopy(this.data.data, var19, var1, var8, var22);
            var8 += var22;
         }
      }

      for(int var14 = var2; var14 < var5; ++var14) {
         for(int var17 = var4; var17 < var7; ++var17) {
            int var20 = (var14 << 11 | var17 << 7 | var3) >> 1;
            int var23 = (var6 - var3) / 2;
            System.arraycopy(this.blockLight.data, var20, var1, var8, var23);
            var8 += var23;
         }
      }

      for(int var15 = var2; var15 < var5; ++var15) {
         for(int var18 = var4; var18 < var7; ++var18) {
            int var21 = (var15 << 11 | var18 << 7 | var3) >> 1;
            int var24 = (var6 - var3) / 2;
            System.arraycopy(this.skyLight.data, var21, var1, var8, var24);
            var8 += var24;
         }
      }

      return var8;
   }

   public int setBlocksAndData(byte[] var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8) {
      for(int var9 = var2; var9 < var5; ++var9) {
         for(int var10 = var4; var10 < var7; ++var10) {
            int var11 = var9 << 11 | var10 << 7 | var3;
            int var12 = var6 - var3;
            System.arraycopy(var1, var8, this.blocks, var11, var12);
            var8 += var12;
         }
      }

      this.recalcHeightmapOnly();

      for(int var13 = var2; var13 < var5; ++var13) {
         for(int var16 = var4; var16 < var7; ++var16) {
            int var19 = (var13 << 11 | var16 << 7 | var3) >> 1;
            int var22 = (var6 - var3) / 2;
            System.arraycopy(var1, var8, this.data.data, var19, var22);
            var8 += var22;
         }
      }

      for(int var14 = var2; var14 < var5; ++var14) {
         for(int var17 = var4; var17 < var7; ++var17) {
            int var20 = (var14 << 11 | var17 << 7 | var3) >> 1;
            int var23 = (var6 - var3) / 2;
            System.arraycopy(var1, var8, this.blockLight.data, var20, var23);
            var8 += var23;
         }
      }

      for(int var15 = var2; var15 < var5; ++var15) {
         for(int var18 = var4; var18 < var7; ++var18) {
            int var21 = (var15 << 11 | var18 << 7 | var3) >> 1;
            int var24 = (var6 - var3) / 2;
            System.arraycopy(var1, var8, this.skyLight.data, var21, var24);
            var8 += var24;
         }
      }

      return var8;
   }

   public Random getRandom(long var1) {
      return new Random(this.level.seed + (long)(this.x * this.x * 4987142) + (long)(this.x * 5947611) + (long)(this.z * this.z) * 4392871L + (long)(this.z * 389711) ^ var1);
   }

   public boolean isEmpty() {
      return false;
   }
}
