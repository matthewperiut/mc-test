package net.minecraft.world.level.chunk.storage;

import com.mojang.nbt.CompoundTag;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityIO;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.tile.entity.TileEntity;

public class ZonedChunkStorage implements ChunkStorage {
   public static final int BIT_TERRAIN_POPULATED = 1;
   public static final int CHUNKS_PER_ZONE_BITS = 5;
   public static final int CHUNKS_PER_ZONE = 32;
   public static final int CHUNK_WIDTH = 16;
   public static final int CHUNK_HEADER_SIZE = 256;
   public static final int CHUNK_SIZE = 32768;
   public static final int CHUNK_LAYERS = 3;
   public static final int CHUNK_SIZE_BYTES = 98560;
   public static final ByteOrder BYTE_ORDER;
   private File dir;
   private Map<Long, ZoneFile> zoneFiles = new HashMap();
   private long tickCount = 0L;

   public ZonedChunkStorage(File var1) {
      var1 = new File(var1, "data");
      if (!var1.exists()) {
         var1.mkdir();
      }

      this.dir = var1;
   }

   private int getSlot(int var1, int var2) {
      int var3 = var1 >> 5;
      int var4 = var2 >> 5;
      int var5 = var1 - (var3 << 5);
      int var6 = var2 - (var4 << 5);
      int var7 = var5 + var6 * 32;
      return var7;
   }

   private ZoneFile getZoneFile(int var1, int var2, boolean var3) throws IOException {
      int var4 = this.getSlot(var1, var2);
      int var5 = var1 >> 5;
      int var6 = var2 >> 5;
      long var7 = (long)(var5 + (var6 << 20));
      if (!this.zoneFiles.containsKey(var7)) {
         File var9 = new File(this.dir, "zone_" + Integer.toString(var1, 36) + "_" + Integer.toString(var2, 36) + ".dat");
         if (!var9.exists()) {
            if (!var3) {
               return null;
            }

            var9.createNewFile();
         }

         File var10 = new File(this.dir, "entities_" + Integer.toString(var1, 36) + "_" + Integer.toString(var2, 36) + ".dat");
         this.zoneFiles.put(var7, new ZoneFile(var7, var9, var10));
      }

      ZoneFile var11 = (ZoneFile)this.zoneFiles.get(var7);
      var11.lastUse = this.tickCount;
      return !var11.containsSlot(var4) && !var3 ? null : var11;
   }

   private ZoneIo getBuffer(int var1, int var2, boolean var3) throws IOException {
      ZoneFile var4 = this.getZoneFile(var1, var2, var3);
      return var4 == null ? null : var4.getZoneIo(this.getSlot(var1, var2));
   }

   public LevelChunk load(Level var1, int var2, int var3) throws IOException {
      ZoneIo var4 = this.getBuffer(var2, var3, false);
      if (var4 == null) {
         return null;
      } else {
         LevelChunk var5 = new LevelChunk(var1, var2, var3);
         var5.unsaved = false;
         ByteBuffer var6 = var4.read(256);
         var5.blocks = var4.read(32768).array();
         var5.data = new DataLayer(var4.read(16384).array());
         var5.skyLight = new DataLayer(var4.read(16384).array());
         var5.blockLight = new DataLayer(var4.read(16384).array());
         var5.heightmap = var4.read(256).array();
         var6.flip();
         int var7 = var6.getInt();
         int var8 = var6.getInt();
         long var9 = var6.getLong();
         long var11 = var6.getLong();
         var5.terrainPopulated = (var11 & 1L) != 0L;
         this.loadEntities(var1, var5);
         return var5;
      }
   }

   public void save(Level var1, LevelChunk var2) throws IOException {
      long var3 = 0L;
      if (var2.terrainPopulated) {
         var3 |= 1L;
      }

      ByteBuffer var5 = ByteBuffer.allocate(256);
      var5.order(BYTE_ORDER);
      var5.putInt(var2.x);
      var5.putInt(var2.z);
      var5.putLong(var1.time);
      var5.putLong(var3);
      var5.flip();
      ZoneIo var6 = this.getBuffer(var2.x, var2.z, true);
      var6.write((ByteBuffer)var5, 256);
      var6.write((byte[])var2.blocks, 32768);
      var6.write((byte[])var2.data.data, 16384);
      var6.write((byte[])var2.skyLight.data, 16384);
      var6.write((byte[])var2.blockLight.data, 16384);
      var6.write((byte[])var2.heightmap, 256);
      var6.flush();
   }

   public void tick() {
      ++this.tickCount;
      if (this.tickCount % 200L == 4L) {
         ArrayList var1 = new ArrayList();

         for(ZoneFile var3 : this.zoneFiles.values()) {
            if (this.tickCount - var3.lastUse > 1200L) {
               var1.add(var3.key);
            }
         }

         for(Object var7obj : var1) {
            Long var7 = (Long)var7obj;
            try {
               System.out.println("Closing zone " + var7);
               ((ZoneFile)this.zoneFiles.get(var7)).close();
               this.zoneFiles.remove(var7);
            } catch (IOException var5) {
               var5.printStackTrace();
            }
         }
      }

   }

   public void flush() {
      for(ZoneFile var2 : this.zoneFiles.values()) {
         try {
            var2.close();
         } catch (IOException var4) {
            var4.printStackTrace();
         }
      }

      this.zoneFiles.clear();
   }

   public void loadEntities(Level var1, LevelChunk var2) throws IOException {
      int var3 = this.getSlot(var2.x, var2.z);
      ZoneFile var4 = this.getZoneFile(var2.x, var2.z, true);
      List var5 = var4.entityFile.readAll(var3);

      for(int var6 = 0; var6 < var5.size(); ++var6) {
         CompoundTag var7 = (CompoundTag)var5.get(var6);
         int var8 = var7.getInt("_TYPE");
         if (var8 == 0) {
            Entity var9 = EntityIO.loadStatic(var7, var1);
            if (var9 != null) {
               var2.addEntity(var9);
            }
         } else if (var8 == 1) {
            TileEntity var10 = TileEntity.loadStatic(var7);
            if (var10 != null) {
               var2.addTileEntity(var10);
            }
         }
      }

   }

   public void saveEntities(Level var1, LevelChunk var2) throws IOException {
      int var3 = this.getSlot(var2.x, var2.z);
      ZoneFile var4 = this.getZoneFile(var2.x, var2.z, true);
      ArrayList var5 = new ArrayList();

      for(int var6 = 0; var6 < var2.entityBlocks.length; ++var6) {
         List var7 = var2.entityBlocks[var6];

         for(int var8 = 0; var8 < var7.size(); ++var8) {
            Entity var9 = (Entity)var7.get(var8);
            CompoundTag var10 = new CompoundTag();
            var10.putInt("_TYPE", 0);
            var9.save(var10);
            var5.add(var10);
         }
      }

      for(TileEntity var12 : var2.tileEntities.values()) {
         CompoundTag var13 = new CompoundTag();
         var13.putInt("_TYPE", 1);
         var12.save(var13);
         var5.add(var13);
      }

      var4.entityFile.replaceSlot(var3, var5);
   }

   static {
      BYTE_ORDER = ByteOrder.BIG_ENDIAN;
   }
}
