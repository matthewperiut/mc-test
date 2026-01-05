package net.minecraft.world.level.chunk.storage;

import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.ListTag;
import com.mojang.nbt.NbtIo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityIO;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.tile.entity.TileEntity;

public class OldChunkStorage implements ChunkStorage {
   private File dir;
   private boolean create;

   public OldChunkStorage(File var1, boolean var2) {
      this.dir = var1;
      this.create = var2;
   }

   private File getFile(int var1, int var2) {
      String var3 = "c." + Integer.toString(var1, 36) + "." + Integer.toString(var2, 36) + ".dat";
      String var4 = Integer.toString(var1 & 63, 36);
      String var5 = Integer.toString(var2 & 63, 36);
      File var6 = new File(this.dir, var4);
      if (!var6.exists()) {
         if (!this.create) {
            return null;
         }

         var6.mkdir();
      }

      var6 = new File(var6, var5);
      if (!var6.exists()) {
         if (!this.create) {
            return null;
         }

         var6.mkdir();
      }

      var6 = new File(var6, var3);
      return !var6.exists() && !this.create ? null : var6;
   }

   public LevelChunk load(Level var1, int var2, int var3) {
      File var4 = this.getFile(var2, var3);
      if (var4 != null && var4.exists()) {
         try {
            FileInputStream var5 = new FileInputStream(var4);
            CompoundTag var6 = NbtIo.readCompressed(var5);
            if (!var6.contains("Level")) {
               System.out.println("Chunk file at " + var2 + "," + var3 + " is missing level data, skipping");
               return null;
            }

            if (!var6.getCompound("Level").contains("Blocks")) {
               System.out.println("Chunk file at " + var2 + "," + var3 + " is missing block data, skipping");
               return null;
            }

            LevelChunk var7 = load(var1, var6.getCompound("Level"));
            if (!var7.isAt(var2, var3)) {
               System.out.println("Chunk file at " + var2 + "," + var3 + " is in the wrong location; relocating. (Expected " + var2 + ", " + var3 + ", got " + var7.x + ", " + var7.z + ")");
               var6.putInt("xPos", var2);
               var6.putInt("zPos", var3);
               var7 = load(var1, var6.getCompound("Level"));
            }

            return var7;
         } catch (Exception var8) {
            var8.printStackTrace();
         }
      }

      return null;
   }

   public void save(Level var1, LevelChunk var2) {
      var1.checkSession();
      File var3 = this.getFile(var2.x, var2.z);
      if (var3.exists()) {
         var1.sizeOnDisk -= var3.length();
      }

      try {
         File var4 = new File(this.dir, "tmp_chunk.dat");
         FileOutputStream var5 = new FileOutputStream(var4);
         CompoundTag var6 = new CompoundTag();
         CompoundTag var7 = new CompoundTag();
         var6.put("Level", var7);
         this.save(var2, var1, var7);
         NbtIo.writeCompressed(var6, var5);
         var5.close();
         if (var3.exists()) {
            var3.delete();
         }

         var4.renameTo(var3);
         var1.sizeOnDisk += var3.length();
      } catch (Exception var8) {
         var8.printStackTrace();
      }

   }

   public void save(LevelChunk var1, Level var2, CompoundTag var3) {
      var2.checkSession();
      var3.putInt("xPos", var1.x);
      var3.putInt("zPos", var1.z);
      var3.putLong("LastUpdate", var2.time);
      var3.putByteArray("Blocks", var1.blocks);
      var3.putByteArray("Data", var1.data.data);
      var3.putByteArray("SkyLight", var1.skyLight.data);
      var3.putByteArray("BlockLight", var1.blockLight.data);
      var3.putByteArray("HeightMap", var1.heightmap);
      var3.putBoolean("TerrainPopulated", var1.terrainPopulated);
      var1.lastSaveHadEntities = false;
      ListTag var4 = new ListTag();

      for(int var5 = 0; var5 < var1.entityBlocks.length; ++var5) {
         for(Entity var7 : var1.entityBlocks[var5]) {
            var1.lastSaveHadEntities = true;
            CompoundTag var8 = new CompoundTag();
            if (var7.save(var8)) {
               var4.add(var8);
            }
         }
      }

      var3.put("Entities", var4);
      ListTag var9 = new ListTag();

      for(TileEntity var11 : var1.tileEntities.values()) {
         CompoundTag var12 = new CompoundTag();
         var11.save(var12);
         var9.add(var12);
      }

      var3.put("TileEntities", var9);
   }

   public static LevelChunk load(Level var0, CompoundTag var1) {
      int var2 = var1.getInt("xPos");
      int var3 = var1.getInt("zPos");
      LevelChunk var4 = new LevelChunk(var0, var2, var3);
      var4.blocks = var1.getByteArray("Blocks");
      var4.data = new DataLayer(var1.getByteArray("Data"));
      var4.skyLight = new DataLayer(var1.getByteArray("SkyLight"));
      var4.blockLight = new DataLayer(var1.getByteArray("BlockLight"));
      var4.heightmap = var1.getByteArray("HeightMap");
      var4.terrainPopulated = var1.getBoolean("TerrainPopulated");
      if (!var4.data.isValid()) {
         var4.data = new DataLayer(var4.blocks.length);
      }

      if (var4.heightmap == null || !var4.skyLight.isValid()) {
         var4.heightmap = new byte[256];
         var4.skyLight = new DataLayer(var4.blocks.length);
         var4.recalcHeightmap();
      }

      if (!var4.blockLight.isValid()) {
         var4.blockLight = new DataLayer(var4.blocks.length);
         var4.recalcBlockLights();
      }

      ListTag var5 = var1.getList("Entities");
      if (var5 != null) {
         for(int var6 = 0; var6 < var5.size(); ++var6) {
            CompoundTag var7 = (CompoundTag)var5.get(var6);
            Entity var8 = EntityIO.loadStatic(var7, var0);
            var4.lastSaveHadEntities = true;
            if (var8 != null) {
               var4.addEntity(var8);
            }
         }
      }

      ListTag var10 = var1.getList("TileEntities");
      if (var10 != null) {
         for(int var11 = 0; var11 < var10.size(); ++var11) {
            CompoundTag var12 = (CompoundTag)var10.get(var11);
            TileEntity var9 = TileEntity.loadStatic(var12);
            if (var9 != null) {
               var4.addTileEntity(var9);
            }
         }
      }

      return var4;
   }

   public void tick() {
   }

   public void flush() {
   }

   public void saveEntities(Level var1, LevelChunk var2) {
   }
}
