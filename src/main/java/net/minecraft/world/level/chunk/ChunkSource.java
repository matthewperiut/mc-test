package net.minecraft.world.level.chunk;

import util.ProgressListener;

public interface ChunkSource {
   boolean hasChunk(int var1, int var2);

   LevelChunk getChunk(int var1, int var2);

   void postProcess(ChunkSource var1, int var2, int var3);

   boolean save(boolean var1, ProgressListener var2);

   boolean tick();

   boolean shouldSave();

   String gatherStats();
}
