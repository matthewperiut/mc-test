package net.minecraft.world.level.chunk;

import java.io.IOException;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import util.ProgressListener;

public class ReadOnlyChunkCache implements ChunkSource {
   private static final int LEN = 16;
   private static final int LEN_MASK = 15;
   private LevelChunk[] chunks = new LevelChunk[256];
   private Level level;
   private ChunkStorage storage;
   byte[] emptyPixels = new byte['\u8000'];

   public ReadOnlyChunkCache(Level var1, ChunkStorage var2) {
      this.level = var1;
      this.storage = var2;
   }

   public boolean hasChunk(int var1, int var2) {
      int var3 = var1 & 15 | (var2 & 15) * 16;
      return this.chunks[var3] != null && this.chunks[var3].isAt(var1, var2);
   }

   public LevelChunk getChunk(int var1, int var2) {
      int var3 = var1 & 15 | (var2 & 15) * 16;

      try {
         if (!this.hasChunk(var1, var2)) {
            Object var4 = this.load(var1, var2);
            if (var4 == null) {
               var4 = new EmptyLevelChunk(this.level, this.emptyPixels, var1, var2);
            }

            this.chunks[var3] = (LevelChunk)var4;
         }

         return this.chunks[var3];
      } catch (Exception var5) {
         var5.printStackTrace();
         return null;
      }
   }

   private synchronized LevelChunk load(int var1, int var2) {
      try {
         return this.storage.load(this.level, var1, var2);
      } catch (IOException var4) {
         var4.printStackTrace();
         return null;
      }
   }

   public void postProcess(ChunkSource var1, int var2, int var3) {
   }

   public boolean save(boolean var1, ProgressListener var2) {
      return true;
   }

   public boolean tick() {
      return false;
   }

   public boolean shouldSave() {
      return false;
   }

   public String gatherStats() {
      return "ReadOnlyChunkCache";
   }
}
