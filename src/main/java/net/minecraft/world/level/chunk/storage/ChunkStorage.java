package net.minecraft.world.level.chunk.storage;

import java.io.IOException;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public interface ChunkStorage {
   LevelChunk load(Level var1, int var2, int var3) throws IOException;

   void save(Level var1, LevelChunk var2) throws IOException;

   void saveEntities(Level var1, LevelChunk var2) throws IOException;

   void tick();

   void flush();
}
