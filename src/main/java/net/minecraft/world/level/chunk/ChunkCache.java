package net.minecraft.world.level.chunk;

import java.io.IOException;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import util.ProgressListener;

public class ChunkCache implements ChunkSource {
   private static final int CHUNK_CACHE_WIDTH = 32;
   private LevelChunk emptyChunk;
   private ChunkSource source;
   private ChunkStorage storage;
   private LevelChunk[] chunks = new LevelChunk[1024];
   private Level level;
   int xLast = -999999999;
   int zLast = -999999999;
   private LevelChunk last;
   private int xCenter;
   private int yCenter;
   private static final int MAX_SAVES = 2;

   public ChunkCache(Level var1, ChunkStorage var2, ChunkSource var3) {
      this.emptyChunk = new EmptyLevelChunk(var1, new byte['\u8000'], 0, 0);
      this.level = var1;
      this.storage = var2;
      this.source = var3;
   }

   public void centerOn(int var1, int var2) {
      this.xCenter = var1;
      this.yCenter = var2;
   }

   public boolean fits(int var1, int var2) {
      byte var3 = 15;
      return var1 >= this.xCenter - var3 && var2 >= this.yCenter - var3 && var1 <= this.xCenter + var3 && var2 <= this.yCenter + var3;
   }

   public boolean hasChunk(int var1, int var2) {
      if (!this.fits(var1, var2)) {
         return false;
      } else if (var1 == this.xLast && var2 == this.zLast && this.last != null) {
         return true;
      } else {
         int var3 = var1 & 31;
         int var4 = var2 & 31;
         int var5 = var3 + var4 * 32;
         return this.chunks[var5] != null && (this.chunks[var5] == this.emptyChunk || this.chunks[var5].isAt(var1, var2));
      }
   }

   public LevelChunk getChunk(int var1, int var2) {
      if (var1 == this.xLast && var2 == this.zLast && this.last != null) {
         return this.last;
      } else if (!this.level.isFindingSpawn && !this.fits(var1, var2)) {
         return this.emptyChunk;
      } else {
         int var3 = var1 & 31;
         int var4 = var2 & 31;
         int var5 = var3 + var4 * 32;
         if (!this.hasChunk(var1, var2)) {
            if (this.chunks[var5] != null) {
               this.chunks[var5].unload();
               this.save(this.chunks[var5]);
               this.saveEntities(this.chunks[var5]);
            }

            LevelChunk var6 = this.load(var1, var2);
            if (var6 == null) {
               if (this.source == null) {
                  var6 = this.emptyChunk;
               } else {
                  var6 = this.source.getChunk(var1, var2);
               }
            }

            this.chunks[var5] = var6;
            var6.lightLava();
            if (this.chunks[var5] != null) {
               this.chunks[var5].load();
            }

            if (!this.chunks[var5].terrainPopulated && this.hasChunk(var1 + 1, var2 + 1) && this.hasChunk(var1, var2 + 1) && this.hasChunk(var1 + 1, var2)) {
               this.postProcess(this, var1, var2);
            }

            if (this.hasChunk(var1 - 1, var2) && !this.getChunk(var1 - 1, var2).terrainPopulated && this.hasChunk(var1 - 1, var2 + 1) && this.hasChunk(var1, var2 + 1) && this.hasChunk(var1 - 1, var2)) {
               this.postProcess(this, var1 - 1, var2);
            }

            if (this.hasChunk(var1, var2 - 1) && !this.getChunk(var1, var2 - 1).terrainPopulated && this.hasChunk(var1 + 1, var2 - 1) && this.hasChunk(var1, var2 - 1) && this.hasChunk(var1 + 1, var2)) {
               this.postProcess(this, var1, var2 - 1);
            }

            if (this.hasChunk(var1 - 1, var2 - 1) && !this.getChunk(var1 - 1, var2 - 1).terrainPopulated && this.hasChunk(var1 - 1, var2 - 1) && this.hasChunk(var1, var2 - 1) && this.hasChunk(var1 - 1, var2)) {
               this.postProcess(this, var1 - 1, var2 - 1);
            }
         }

         this.xLast = var1;
         this.zLast = var2;
         this.last = this.chunks[var5];
         return this.chunks[var5];
      }
   }

   private LevelChunk load(int var1, int var2) {
      if (this.storage == null) {
         return this.emptyChunk;
      } else {
         try {
            LevelChunk var3 = this.storage.load(this.level, var1, var2);
            if (var3 != null) {
               var3.lastSaveTime = this.level.time;
            }

            return var3;
         } catch (Exception var4) {
            var4.printStackTrace();
            return this.emptyChunk;
         }
      }
   }

   private void saveEntities(LevelChunk var1) {
      if (this.storage != null) {
         try {
            this.storage.saveEntities(this.level, var1);
         } catch (Exception var3) {
            var3.printStackTrace();
         }

      }
   }

   private void save(LevelChunk var1) {
      if (this.storage != null) {
         try {
            var1.lastSaveTime = this.level.time;
            this.storage.save(this.level, var1);
         } catch (IOException var3) {
            var3.printStackTrace();
         }

      }
   }

   public void postProcess(ChunkSource var1, int var2, int var3) {
      LevelChunk var4 = this.getChunk(var2, var3);
      if (!var4.terrainPopulated) {
         var4.terrainPopulated = true;
         if (this.source != null) {
            this.source.postProcess(var1, var2, var3);
            var4.markUnsaved();
         }
      }

   }

   public boolean save(boolean var1, ProgressListener var2) {
      int var3 = 0;
      int var4 = 0;
      if (var2 != null) {
         for(int var5 = 0; var5 < this.chunks.length; ++var5) {
            if (this.chunks[var5] != null && this.chunks[var5].shouldSave(var1)) {
               ++var4;
            }
         }
      }

      int var7 = 0;

      for(int var6 = 0; var6 < this.chunks.length; ++var6) {
         if (this.chunks[var6] != null) {
            if (var1 && !this.chunks[var6].dontSave) {
               this.saveEntities(this.chunks[var6]);
            }

            if (this.chunks[var6].shouldSave(var1)) {
               this.save(this.chunks[var6]);
               this.chunks[var6].unsaved = false;
               ++var3;
               if (var3 == 2 && !var1) {
                  return false;
               }

               if (var2 != null) {
                  ++var7;
                  if (var7 % 10 == 0) {
                     var2.progressStagePercentage(var7 * 100 / var4);
                  }
               }
            }
         }
      }

      if (var1) {
         if (this.storage == null) {
            return true;
         }

         this.storage.flush();
      }

      return true;
   }

   public boolean tick() {
      if (this.storage != null) {
         this.storage.tick();
      }

      return this.source.tick();
   }

   public boolean shouldSave() {
      return true;
   }

   public String gatherStats() {
      return "ChunkCache: " + this.chunks.length;
   }
}
