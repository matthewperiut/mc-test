package net.minecraft.world.level.chunk.storage;

import java.io.IOException;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public class CompatibilityChunkStorage implements ChunkStorage {
   private ChunkStorage primary;
   private ChunkStorage secondary;

   public CompatibilityChunkStorage(ChunkStorage var1, ChunkStorage var2) {
      this.primary = var1;
      this.secondary = var2;
   }

   public void flush() {
      this.primary.flush();
   }

   public LevelChunk load(Level var1, int var2, int var3) throws IOException {
      LevelChunk var4 = this.primary.load(var1, var2, var3);
      if (var4 == null) {
         var4 = this.secondary.load(var1, var2, var3);
         if (var4 != null) {
            var4.unsaved = true;
         }
      }

      return var4;
   }

   public void save(Level var1, LevelChunk var2) throws IOException {
      this.primary.save(var1, var2);
   }

   public void tick() {
      this.primary.tick();
   }

   public void saveEntities(Level var1, LevelChunk var2) throws IOException {
      this.primary.saveEntities(var1, var2);
   }
}
