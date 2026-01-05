package net.minecraft.client.multiplayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import util.ProgressListener;

public class MultiPlayerChunkCache implements ChunkSource {
   private LevelChunk emptyChunk;
   private Map<ChunkPos, LevelChunk> loadedChunks = new HashMap();
   private List<LevelChunk> loadedChunkList = new ArrayList();
   private Level level;

   public MultiPlayerChunkCache(Level level) {
      this.emptyChunk = new EmptyLevelChunk(level, new byte['\u8000'], 0, 0);
      this.level = level;
   }

   public boolean hasChunk(int x, int z) {
      ChunkPos chunkPos = new ChunkPos(x, z);
      return this.loadedChunks.containsKey(chunkPos);
   }

   public void drop(int x, int z) {
      LevelChunk chunk = this.getChunk(x, z);
      if (!chunk.isEmpty()) {
         chunk.unload();
      }

      this.loadedChunks.remove(new ChunkPos(x, z));
      this.loadedChunkList.remove(chunk);
   }

   public LevelChunk create(int x, int z) {
      ChunkPos pos = new ChunkPos(x, z);
      byte[] bytes = new byte['\u8000'];
      LevelChunk chunk = new LevelChunk(this.level, bytes, x, z);
      Arrays.fill(chunk.skyLight.data, (byte)-1);
      this.loadedChunks.put(pos, chunk);
      chunk.loaded = true;
      return chunk;
   }

   public LevelChunk getChunk(int x, int z) {
      ChunkPos pos = new ChunkPos(x, z);
      LevelChunk chunk = (LevelChunk)this.loadedChunks.get(pos);
      return chunk == null ? this.emptyChunk : chunk;
   }

   public boolean save(boolean force, ProgressListener progressListener) {
      return true;
   }

   public boolean tick() {
      return false;
   }

   public boolean shouldSave() {
      return false;
   }

   public void postProcess(ChunkSource parent, int x, int z) {
   }

   public String gatherStats() {
      return "MultiplayerChunkCache: " + this.loadedChunks.size();
   }

   private static final class ChunkPos {
      public final int x;
      public final int z;

      public ChunkPos(int x, int z) {
         this.x = x;
         this.z = z;
      }

      public boolean equals(Object obj) {
         if (obj instanceof ChunkPos) {
            ChunkPos cp = (ChunkPos)obj;
            return this.x == cp.x && this.z == cp.z;
         } else {
            return false;
         }
      }

      public int hashCode() {
         return this.x << 16 ^ this.z;
      }
   }
}
