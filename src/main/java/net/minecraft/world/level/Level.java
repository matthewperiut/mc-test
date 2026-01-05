package net.minecraft.world.level;

import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.NbtIo;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkCache;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.dimension.HellDimension;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.tile.LiquidTile;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import util.Mth;
import util.ProgressListener;

public class Level implements LevelSource {
   private static final int MAX_TICK_TILES_PER_TICK = 1000;
   public static final int MAX_LEVEL_SIZE = 32000000;
   public static final short DEPTH = 128;
   public static final short SEA_LEVEL = 63;
   public boolean instaTick;
   public static final int MAX_BRIGHTNESS = 15;
   public static final int TICKS_PER_DAY = 24000;
   private List<LightUpdate> lightUpdates;
   public List<Entity> entities;
   private List<Entity> entitiesToRemove;
   private TreeSet<TickNextTickData> tickNextTickList;
   private Set<TickNextTickData> tickNextTickSet;
   public List<TileEntity> tileEntityList;
   public List<Player> players;
   public long time;
   private long cloudColor;
   public int skyDarken;
   protected int randValue;
   protected int addend;
   public boolean noNeighborUpdate;
   private long sessionId;
   protected int saveInterval;
   public int difficulty;
   public Random random;
   public int xSpawn;
   public int ySpawn;
   public int zSpawn;
   public boolean isNew;
   public final Dimension dimension;
   protected List<LevelListener> listeners;
   private ChunkSource chunkSource;
   public File workDir;
   public File dir;
   public long seed;
   private CompoundTag loadedPlayerTag;
   public long sizeOnDisk;
   public final String name;
   public boolean isFindingSpawn;
   private ArrayList<AABB> boxes;
   private int maxRecurse;
   private boolean spawnEnemies;
   private boolean spawnFriendlies;
   static int maxLoop = 0;
   private Set<ChunkPos> chunksToPoll;
   private int delayUntilNextMoodSound;
   private List<Entity> es;
   public boolean isOnline;

   public static CompoundTag getDataTagFor(File var0, String var1) {
      File var2 = new File(var0, "saves");
      File var3 = new File(var2, var1);
      if (!var3.exists()) {
         return null;
      } else {
         File var4 = new File(var3, "level.dat");
         if (var4.exists()) {
            try {
               CompoundTag var5 = NbtIo.readCompressed(new FileInputStream(var4));
               CompoundTag var6 = var5.getCompound("Data");
               return var6;
            } catch (Exception var7) {
               var7.printStackTrace();
            }
         }

         return null;
      }
   }

   public static void deleteLevel(File var0, String var1) {
      File var2 = new File(var0, "saves");
      File var3 = new File(var2, var1);
      if (var3.exists()) {
         delete(var3.listFiles());
         var3.delete();
      }
   }

   private static void delete(File[] var0) {
      for(int var1 = 0; var1 < var0.length; ++var1) {
         if (var0[var1].isDirectory()) {
            delete(var0[var1].listFiles());
         }

         var0[var1].delete();
      }

   }

   public BiomeSource getBiomeSource() {
      return this.dimension.biomeSource;
   }

   public static long getLevelSize(File var0, String var1) {
      File var2 = new File(var0, "saves");
      File var3 = new File(var2, var1);
      return !var3.exists() ? 0L : calcSize(var3.listFiles());
   }

   private static long calcSize(File[] var0) {
      long var1 = 0L;

      for(int var3 = 0; var3 < var0.length; ++var3) {
         if (var0[var3].isDirectory()) {
            var1 += calcSize(var0[var3].listFiles());
         } else {
            var1 += var0[var3].length();
         }
      }

      return var1;
   }

   public Level(File var1, String var2) {
      this(var1, var2, (new Random()).nextLong());
   }

   public Level(String var1, Dimension var2, long var3) {
      this.instaTick = false;
      this.lightUpdates = new ArrayList();
      this.entities = new ArrayList();
      this.entitiesToRemove = new ArrayList();
      this.tickNextTickList = new TreeSet();
      this.tickNextTickSet = new HashSet();
      this.tileEntityList = new ArrayList();
      this.players = new ArrayList();
      this.time = 0L;
      this.cloudColor = 16777215L;
      this.skyDarken = 0;
      this.randValue = (new Random()).nextInt();
      this.addend = 1013904223;
      this.noNeighborUpdate = false;
      this.sessionId = System.currentTimeMillis();
      this.saveInterval = 40;
      this.random = new Random();
      this.isNew = false;
      this.listeners = new ArrayList();
      this.seed = 0L;
      this.sizeOnDisk = 0L;
      this.boxes = new ArrayList();
      this.maxRecurse = 0;
      this.spawnEnemies = true;
      this.spawnFriendlies = true;
      this.chunksToPoll = new HashSet();
      this.delayUntilNextMoodSound = this.random.nextInt(12000);
      this.es = new ArrayList();
      this.isOnline = false;
      this.name = var1;
      this.seed = var3;
      this.dimension = var2;
      var2.init(this);
      this.chunkSource = this.createChunkSource(this.dir);
      this.updateSkyBrightness();
   }

   public Level(Level var1, Dimension var2) {
      this.instaTick = false;
      this.lightUpdates = new ArrayList();
      this.entities = new ArrayList();
      this.entitiesToRemove = new ArrayList();
      this.tickNextTickList = new TreeSet();
      this.tickNextTickSet = new HashSet();
      this.tileEntityList = new ArrayList();
      this.players = new ArrayList();
      this.time = 0L;
      this.cloudColor = 16777215L;
      this.skyDarken = 0;
      this.randValue = (new Random()).nextInt();
      this.addend = 1013904223;
      this.noNeighborUpdate = false;
      this.sessionId = System.currentTimeMillis();
      this.saveInterval = 40;
      this.random = new Random();
      this.isNew = false;
      this.listeners = new ArrayList();
      this.seed = 0L;
      this.sizeOnDisk = 0L;
      this.boxes = new ArrayList();
      this.maxRecurse = 0;
      this.spawnEnemies = true;
      this.spawnFriendlies = true;
      this.chunksToPoll = new HashSet();
      this.delayUntilNextMoodSound = this.random.nextInt(12000);
      this.es = new ArrayList();
      this.isOnline = false;
      this.sessionId = var1.sessionId;
      this.workDir = var1.workDir;
      this.dir = var1.dir;
      this.name = var1.name;
      this.seed = var1.seed;
      this.time = var1.time;
      this.xSpawn = var1.xSpawn;
      this.ySpawn = var1.ySpawn;
      this.zSpawn = var1.zSpawn;
      this.sizeOnDisk = var1.sizeOnDisk;
      this.dimension = var2;
      var2.init(this);
      this.chunkSource = this.createChunkSource(this.dir);
      this.updateSkyBrightness();
   }

   public Level(File var1, String var2, long var3) {
      this(var1, var2, var3, (Dimension)null);
   }

   public Level(File var1, String var2, long var3, Dimension var5) {
      this.instaTick = false;
      this.lightUpdates = new ArrayList();
      this.entities = new ArrayList();
      this.entitiesToRemove = new ArrayList();
      this.tickNextTickList = new TreeSet();
      this.tickNextTickSet = new HashSet();
      this.tileEntityList = new ArrayList();
      this.players = new ArrayList();
      this.time = 0L;
      this.cloudColor = 16777215L;
      this.skyDarken = 0;
      this.randValue = (new Random()).nextInt();
      this.addend = 1013904223;
      this.noNeighborUpdate = false;
      this.sessionId = System.currentTimeMillis();
      this.saveInterval = 40;
      this.random = new Random();
      this.isNew = false;
      this.listeners = new ArrayList();
      this.seed = 0L;
      this.sizeOnDisk = 0L;
      this.boxes = new ArrayList();
      this.maxRecurse = 0;
      this.spawnEnemies = true;
      this.spawnFriendlies = true;
      this.chunksToPoll = new HashSet();
      this.delayUntilNextMoodSound = this.random.nextInt(12000);
      this.es = new ArrayList();
      this.isOnline = false;
      this.workDir = var1;
      this.name = var2;
      var1.mkdirs();
      this.dir = new File(var1, var2);
      this.dir.mkdirs();

      try {
         File var6 = new File(this.dir, "session.lock");
         DataOutputStream var7 = new DataOutputStream(new FileOutputStream(var6));

         try {
            var7.writeLong(this.sessionId);
         } finally {
            var7.close();
         }
      } catch (IOException var16) {
         var16.printStackTrace();
         throw new RuntimeException("Failed to check session lock, aborting");
      }

      Object var17 = new Dimension();
      File var18 = new File(this.dir, "level.dat");
      this.isNew = !var18.exists();
      if (var18.exists()) {
         try {
            CompoundTag var8 = NbtIo.readCompressed(new FileInputStream(var18));
            CompoundTag var9 = var8.getCompound("Data");
            this.seed = var9.getLong("RandomSeed");
            this.xSpawn = var9.getInt("SpawnX");
            this.ySpawn = var9.getInt("SpawnY");
            this.zSpawn = var9.getInt("SpawnZ");
            this.time = var9.getLong("Time");
            this.sizeOnDisk = var9.getLong("SizeOnDisk");
            if (var9.contains("Player")) {
               this.loadedPlayerTag = var9.getCompound("Player");
               int var10 = this.loadedPlayerTag.getInt("Dimension");
               if (var10 == -1) {
                  var17 = new HellDimension();
               }
            }
         } catch (Exception var14) {
            var14.printStackTrace();
         }
      }

      if (var5 != null) {
         var17 = var5;
      }

      boolean var19 = false;
      if (this.seed == 0L) {
         this.seed = var3;
         var19 = true;
      }

      this.dimension = (Dimension)var17;
      this.dimension.init(this);
      this.chunkSource = this.createChunkSource(this.dir);
      if (var19) {
         this.isFindingSpawn = true;
         this.xSpawn = 0;
         this.ySpawn = 64;

         for(this.zSpawn = 0; !this.dimension.isValidSpawn(this.xSpawn, this.zSpawn); this.zSpawn += this.random.nextInt(64) - this.random.nextInt(64)) {
            this.xSpawn += this.random.nextInt(64) - this.random.nextInt(64);
         }

         this.isFindingSpawn = false;
      }

      this.updateSkyBrightness();
   }

   protected ChunkSource createChunkSource(File var1) {
      return new ChunkCache(this, this.dimension.createStorage(var1), this.dimension.createRandomLevelSource());
   }

   public void validateSpawn() {
      if (this.ySpawn <= 0) {
         this.ySpawn = 64;
      }

      while(this.getTopTile(this.xSpawn, this.zSpawn) == 0) {
         this.xSpawn += this.random.nextInt(8) - this.random.nextInt(8);
         this.zSpawn += this.random.nextInt(8) - this.random.nextInt(8);
      }

   }

   public int getTopTile(int var1, int var2) {
      int var3;
      for(var3 = 63; !this.isEmptyTile(var1, var3 + 1, var2); ++var3) {
      }

      return this.getTile(var1, var3, var2);
   }

   public void clearLoadedPlayerData() {
   }

   public void loadPlayer(Player var1) {
      try {
         if (this.loadedPlayerTag != null) {
            var1.load(this.loadedPlayerTag);
            this.loadedPlayerTag = null;
         }

         if (this.chunkSource instanceof ChunkCache) {
            ChunkCache var2 = (ChunkCache)this.chunkSource;
            int var3 = Mth.floor((float)((int)var1.x)) >> 4;
            int var4 = Mth.floor((float)((int)var1.z)) >> 4;
            var2.centerOn(var3, var4);
         }

         this.addEntity(var1);
      } catch (Exception var5) {
         var5.printStackTrace();
      }

   }

   public void save(boolean var1, ProgressListener var2) {
      if (this.chunkSource.shouldSave()) {
         if (var2 != null) {
            var2.progressStartNoAbort("Saving level");
         }

         this.saveLevelData();
         if (var2 != null) {
            var2.progressStage("Saving chunks");
         }

         this.chunkSource.save(var1, var2);
      }
   }

   private void saveLevelData() {
      this.checkSession();
      CompoundTag var1 = new CompoundTag();
      var1.putLong("RandomSeed", this.seed);
      var1.putInt("SpawnX", this.xSpawn);
      var1.putInt("SpawnY", this.ySpawn);
      var1.putInt("SpawnZ", this.zSpawn);
      var1.putLong("Time", this.time);
      var1.putLong("SizeOnDisk", this.sizeOnDisk);
      var1.putLong("LastPlayed", System.currentTimeMillis());
      Player var2 = null;
      if (this.players.size() > 0) {
         var2 = (Player)this.players.get(0);
      }

      if (var2 != null) {
         CompoundTag var3 = new CompoundTag();
         var2.saveWithoutId(var3);
         var1.putCompound("Player", var3);
      }

      CompoundTag var8 = new CompoundTag();
      var8.put("Data", var1);

      try {
         File var4 = new File(this.dir, "level.dat_new");
         File var5 = new File(this.dir, "level.dat_old");
         File var6 = new File(this.dir, "level.dat");
         NbtIo.writeCompressed(var8, new FileOutputStream(var4));
         if (var5.exists()) {
            var5.delete();
         }

         var6.renameTo(var5);
         if (var6.exists()) {
            var6.delete();
         }

         var4.renameTo(var6);
         if (var4.exists()) {
            var4.delete();
         }
      } catch (Exception var7) {
         var7.printStackTrace();
      }

   }

   public boolean pauseSave(int var1) {
      if (!this.chunkSource.shouldSave()) {
         return true;
      } else {
         if (var1 == 0) {
            this.saveLevelData();
         }

         return this.chunkSource.save(false, (ProgressListener)null);
      }
   }

   public int getTile(int var1, int var2, int var3) {
      if (var1 >= -32000000 && var3 >= -32000000 && var1 < 32000000 && var3 <= 32000000) {
         if (var2 < 0) {
            return 0;
         } else {
            return var2 >= 128 ? 0 : this.getChunk(var1 >> 4, var3 >> 4).getTile(var1 & 15, var2, var3 & 15);
         }
      } else {
         return 0;
      }
   }

   public boolean isEmptyTile(int var1, int var2, int var3) {
      return this.getTile(var1, var2, var3) == 0;
   }

   public boolean hasChunkAt(int var1, int var2, int var3) {
      return var2 >= 0 && var2 < 128 ? this.hasChunk(var1 >> 4, var3 >> 4) : false;
   }

   public boolean hasChunksAt(int var1, int var2, int var3, int var4) {
      return this.hasChunksAt(var1 - var4, var2 - var4, var3 - var4, var1 + var4, var2 + var4, var3 + var4);
   }

   public boolean hasChunksAt(int var1, int var2, int var3, int var4, int var5, int var6) {
      if (var5 >= 0 && var2 < 128) {
         var1 >>= 4;
         var2 >>= 4;
         var3 >>= 4;
         var4 >>= 4;
         var5 >>= 4;
         var6 >>= 4;

         for(int var7 = var1; var7 <= var4; ++var7) {
            for(int var8 = var3; var8 <= var6; ++var8) {
               if (!this.hasChunk(var7, var8)) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean hasChunk(int var1, int var2) {
      return this.chunkSource.hasChunk(var1, var2);
   }

   public LevelChunk getChunkAt(int var1, int var2) {
      return this.getChunk(var1 >> 4, var2 >> 4);
   }

   public LevelChunk getChunk(int var1, int var2) {
      return this.chunkSource.getChunk(var1, var2);
   }

   public boolean setTileAndDataNoUpdate(int var1, int var2, int var3, int var4, int var5) {
      if (var1 >= -32000000 && var3 >= -32000000 && var1 < 32000000 && var3 <= 32000000) {
         if (var2 < 0) {
            return false;
         } else if (var2 >= 128) {
            return false;
         } else {
            LevelChunk var6 = this.getChunk(var1 >> 4, var3 >> 4);
            return var6.setTileAndData(var1 & 15, var2, var3 & 15, var4, var5);
         }
      } else {
         return false;
      }
   }

   public boolean setTileNoUpdate(int var1, int var2, int var3, int var4) {
      if (var1 >= -32000000 && var3 >= -32000000 && var1 < 32000000 && var3 <= 32000000) {
         if (var2 < 0) {
            return false;
         } else if (var2 >= 128) {
            return false;
         } else {
            LevelChunk var5 = this.getChunk(var1 >> 4, var3 >> 4);
            return var5.setTile(var1 & 15, var2, var3 & 15, var4);
         }
      } else {
         return false;
      }
   }

   public Material getMaterial(int var1, int var2, int var3) {
      int var4 = this.getTile(var1, var2, var3);
      return var4 == 0 ? Material.air : Tile.tiles[var4].material;
   }

   public int getData(int var1, int var2, int var3) {
      if (var1 >= -32000000 && var3 >= -32000000 && var1 < 32000000 && var3 <= 32000000) {
         if (var2 < 0) {
            return 0;
         } else if (var2 >= 128) {
            return 0;
         } else {
            LevelChunk var4 = this.getChunk(var1 >> 4, var3 >> 4);
            var1 &= 15;
            var3 &= 15;
            return var4.getData(var1, var2, var3);
         }
      } else {
         return 0;
      }
   }

   public void setData(int var1, int var2, int var3, int var4) {
      if (this.setDataNoUpdate(var1, var2, var3, var4)) {
         this.tileUpdated(var1, var2, var3, this.getTile(var1, var2, var3));
      }

   }

   public boolean setDataNoUpdate(int var1, int var2, int var3, int var4) {
      if (var1 >= -32000000 && var3 >= -32000000 && var1 < 32000000 && var3 <= 32000000) {
         if (var2 < 0) {
            return false;
         } else if (var2 >= 128) {
            return false;
         } else {
            LevelChunk var5 = this.getChunk(var1 >> 4, var3 >> 4);
            var1 &= 15;
            var3 &= 15;
            var5.setData(var1, var2, var3, var4);
            return true;
         }
      } else {
         return false;
      }
   }

   public boolean setTile(int var1, int var2, int var3, int var4) {
      if (this.setTileNoUpdate(var1, var2, var3, var4)) {
         this.tileUpdated(var1, var2, var3, var4);
         return true;
      } else {
         return false;
      }
   }

   public boolean setTileAndData(int var1, int var2, int var3, int var4, int var5) {
      if (this.setTileAndDataNoUpdate(var1, var2, var3, var4, var5)) {
         this.tileUpdated(var1, var2, var3, var4);
         return true;
      } else {
         return false;
      }
   }

   public void sendTileUpdated(int var1, int var2, int var3) {
      for(int var4 = 0; var4 < this.listeners.size(); ++var4) {
         ((LevelListener)this.listeners.get(var4)).tileChanged(var1, var2, var3);
      }

   }

   protected void tileUpdated(int var1, int var2, int var3, int var4) {
      this.sendTileUpdated(var1, var2, var3);
      this.updateNeighborsAt(var1, var2, var3, var4);
   }

   public void lightColumnChanged(int var1, int var2, int var3, int var4) {
      if (var3 > var4) {
         int var5 = var4;
         var4 = var3;
         var3 = var5;
      }

      this.setTilesDirty(var1, var3, var2, var1, var4, var2);
   }

   public void setTileDirty(int var1, int var2, int var3) {
      for(int var4 = 0; var4 < this.listeners.size(); ++var4) {
         ((LevelListener)this.listeners.get(var4)).setTilesDirty(var1, var2, var3, var1, var2, var3);
      }

   }

   public void setTilesDirty(int var1, int var2, int var3, int var4, int var5, int var6) {
      for(int var7 = 0; var7 < this.listeners.size(); ++var7) {
         ((LevelListener)this.listeners.get(var7)).setTilesDirty(var1, var2, var3, var4, var5, var6);
      }

   }

   public void swap(int var1, int var2, int var3, int var4, int var5, int var6) {
      int var7 = this.getTile(var1, var2, var3);
      int var8 = this.getData(var1, var2, var3);
      int var9 = this.getTile(var4, var5, var6);
      int var10 = this.getData(var4, var5, var6);
      this.setTileAndDataNoUpdate(var1, var2, var3, var9, var10);
      this.setTileAndDataNoUpdate(var4, var5, var6, var7, var8);
      this.updateNeighborsAt(var1, var2, var3, var9);
      this.updateNeighborsAt(var4, var5, var6, var7);
   }

   public void updateNeighborsAt(int var1, int var2, int var3, int var4) {
      this.neighborChanged(var1 - 1, var2, var3, var4);
      this.neighborChanged(var1 + 1, var2, var3, var4);
      this.neighborChanged(var1, var2 - 1, var3, var4);
      this.neighborChanged(var1, var2 + 1, var3, var4);
      this.neighborChanged(var1, var2, var3 - 1, var4);
      this.neighborChanged(var1, var2, var3 + 1, var4);
   }

   private void neighborChanged(int var1, int var2, int var3, int var4) {
      if (!this.noNeighborUpdate && !this.isOnline) {
         Tile var5 = Tile.tiles[this.getTile(var1, var2, var3)];
         if (var5 != null) {
            var5.neighborChanged(this, var1, var2, var3, var4);
         }

      }
   }

   public boolean canSeeSky(int var1, int var2, int var3) {
      return this.getChunk(var1 >> 4, var3 >> 4).isSkyLit(var1 & 15, var2, var3 & 15);
   }

   public int getRawBrightness(int var1, int var2, int var3) {
      return this.getRawBrightness(var1, var2, var3, true);
   }

   public int getRawBrightness(int var1, int var2, int var3, boolean var4) {
      if (var1 >= -32000000 && var3 >= -32000000 && var1 < 32000000 && var3 <= 32000000) {
         if (var4) {
            int var5 = this.getTile(var1, var2, var3);
            if (var5 == Tile.stoneSlabHalf.id || var5 == Tile.farmland.id) {
               int var6 = this.getRawBrightness(var1, var2 + 1, var3, false);
               int var7 = this.getRawBrightness(var1 + 1, var2, var3, false);
               int var8 = this.getRawBrightness(var1 - 1, var2, var3, false);
               int var9 = this.getRawBrightness(var1, var2, var3 + 1, false);
               int var10 = this.getRawBrightness(var1, var2, var3 - 1, false);
               if (var7 > var6) {
                  var6 = var7;
               }

               if (var8 > var6) {
                  var6 = var8;
               }

               if (var9 > var6) {
                  var6 = var9;
               }

               if (var10 > var6) {
                  var6 = var10;
               }

               return var6;
            }
         }

         if (var2 < 0) {
            return 0;
         } else if (var2 >= 128) {
            int var14 = 15 - this.skyDarken;
            if (var14 < 0) {
               var14 = 0;
            }

            return var14;
         } else {
            LevelChunk var13 = this.getChunk(var1 >> 4, var3 >> 4);
            var1 &= 15;
            var3 &= 15;
            return var13.getRawBrightness(var1, var2, var3, this.skyDarken);
         }
      } else {
         return 15;
      }
   }

   public boolean isSkyLit(int var1, int var2, int var3) {
      if (var1 >= -32000000 && var3 >= -32000000 && var1 < 32000000 && var3 <= 32000000) {
         if (var2 < 0) {
            return false;
         } else if (var2 >= 128) {
            return true;
         } else if (!this.hasChunk(var1 >> 4, var3 >> 4)) {
            return false;
         } else {
            LevelChunk var4 = this.getChunk(var1 >> 4, var3 >> 4);
            var1 &= 15;
            var3 &= 15;
            return var4.isSkyLit(var1, var2, var3);
         }
      } else {
         return false;
      }
   }

   public int getHeightmap(int var1, int var2) {
      if (var1 >= -32000000 && var2 >= -32000000 && var1 < 32000000 && var2 <= 32000000) {
         if (!this.hasChunk(var1 >> 4, var2 >> 4)) {
            return 0;
         } else {
            LevelChunk var3 = this.getChunk(var1 >> 4, var2 >> 4);
            return var3.getHeightmap(var1 & 15, var2 & 15);
         }
      } else {
         return 0;
      }
   }

   public void updateLightIfOtherThan(LightLayer var1, int var2, int var3, int var4, int var5) {
      if (!this.dimension.hasCeiling || var1 != LightLayer.Sky) {
         if (this.hasChunkAt(var2, var3, var4)) {
            if (var1 == LightLayer.Sky) {
               if (this.isSkyLit(var2, var3, var4)) {
                  var5 = 15;
               }
            } else if (var1 == LightLayer.Block) {
               int var6 = this.getTile(var2, var3, var4);
               if (Tile.lightEmission[var6] > var5) {
                  var5 = Tile.lightEmission[var6];
               }
            }

            if (this.getBrightness(var1, var2, var3, var4) != var5) {
               this.updateLight(var1, var2, var3, var4, var2, var3, var4);
            }

         }
      }
   }

   public int getBrightness(LightLayer var1, int var2, int var3, int var4) {
      if (var3 >= 0 && var3 < 128 && var2 >= -32000000 && var4 >= -32000000 && var2 < 32000000 && var4 <= 32000000) {
         int var5 = var2 >> 4;
         int var6 = var4 >> 4;
         if (!this.hasChunk(var5, var6)) {
            return 0;
         } else {
            LevelChunk var7 = this.getChunk(var5, var6);
            return var7.getBrightness(var1, var2 & 15, var3, var4 & 15);
         }
      } else {
         return var1.surrounding;
      }
   }

   public void setBrightness(LightLayer var1, int var2, int var3, int var4, int var5) {
      if (var2 >= -32000000 && var4 >= -32000000 && var2 < 32000000 && var4 <= 32000000) {
         if (var3 >= 0) {
            if (var3 < 128) {
               if (this.hasChunk(var2 >> 4, var4 >> 4)) {
                  LevelChunk var6 = this.getChunk(var2 >> 4, var4 >> 4);
                  var6.setBrightness(var1, var2 & 15, var3, var4 & 15, var5);

                  for(int var7 = 0; var7 < this.listeners.size(); ++var7) {
                     ((LevelListener)this.listeners.get(var7)).tileChanged(var2, var3, var4);
                  }

               }
            }
         }
      }
   }

   public float getBrightness(int var1, int var2, int var3) {
      return this.dimension.brightnessRamp[this.getRawBrightness(var1, var2, var3)];
   }

   public boolean isDay() {
      return this.skyDarken < 4;
   }

   public HitResult clip(Vec3 var1, Vec3 var2) {
      return this.clip(var1, var2, false);
   }

   public HitResult clip(Vec3 var1, Vec3 var2, boolean var3) {
      if (!Double.isNaN(var1.x) && !Double.isNaN(var1.y) && !Double.isNaN(var1.z)) {
         if (!Double.isNaN(var2.x) && !Double.isNaN(var2.y) && !Double.isNaN(var2.z)) {
            int var4 = Mth.floor(var2.x);
            int var5 = Mth.floor(var2.y);
            int var6 = Mth.floor(var2.z);
            int var7 = Mth.floor(var1.x);
            int var8 = Mth.floor(var1.y);
            int var9 = Mth.floor(var1.z);
            int var10 = 200;

            while(var10-- >= 0) {
               if (Double.isNaN(var1.x) || Double.isNaN(var1.y) || Double.isNaN(var1.z)) {
                  return null;
               }

               if (var7 == var4 && var8 == var5 && var9 == var6) {
                  return null;
               }

               double var11 = (double)999.0F;
               double var13 = (double)999.0F;
               double var15 = (double)999.0F;
               if (var4 > var7) {
                  var11 = (double)var7 + (double)1.0F;
               }

               if (var4 < var7) {
                  var11 = (double)var7 + (double)0.0F;
               }

               if (var5 > var8) {
                  var13 = (double)var8 + (double)1.0F;
               }

               if (var5 < var8) {
                  var13 = (double)var8 + (double)0.0F;
               }

               if (var6 > var9) {
                  var15 = (double)var9 + (double)1.0F;
               }

               if (var6 < var9) {
                  var15 = (double)var9 + (double)0.0F;
               }

               double var17 = (double)999.0F;
               double var19 = (double)999.0F;
               double var21 = (double)999.0F;
               double var23 = var2.x - var1.x;
               double var25 = var2.y - var1.y;
               double var27 = var2.z - var1.z;
               if (var11 != (double)999.0F) {
                  var17 = (var11 - var1.x) / var23;
               }

               if (var13 != (double)999.0F) {
                  var19 = (var13 - var1.y) / var25;
               }

               if (var15 != (double)999.0F) {
                  var21 = (var15 - var1.z) / var27;
               }

               byte var29 = 0;
               if (var17 < var19 && var17 < var21) {
                  if (var4 > var7) {
                     var29 = 4;
                  } else {
                     var29 = 5;
                  }

                  var1.x = var11;
                  var1.y += var25 * var17;
                  var1.z += var27 * var17;
               } else if (var19 < var21) {
                  if (var5 > var8) {
                     var29 = 0;
                  } else {
                     var29 = 1;
                  }

                  var1.x += var23 * var19;
                  var1.y = var13;
                  var1.z += var27 * var19;
               } else {
                  if (var6 > var9) {
                     var29 = 2;
                  } else {
                     var29 = 3;
                  }

                  var1.x += var23 * var21;
                  var1.y += var25 * var21;
                  var1.z = var15;
               }

               Vec3 var30 = Vec3.newTemp(var1.x, var1.y, var1.z);
               var7 = (int)(var30.x = (double)Mth.floor(var1.x));
               if (var29 == 5) {
                  --var7;
                  ++var30.x;
               }

               var8 = (int)(var30.y = (double)Mth.floor(var1.y));
               if (var29 == 1) {
                  --var8;
                  ++var30.y;
               }

               var9 = (int)(var30.z = (double)Mth.floor(var1.z));
               if (var29 == 3) {
                  --var9;
                  ++var30.z;
               }

               int var31 = this.getTile(var7, var8, var9);
               int var32 = this.getData(var7, var8, var9);
               Tile var33 = Tile.tiles[var31];
               if (var31 > 0 && var33.mayPick(var32, var3)) {
                  HitResult var34 = var33.clip(this, var7, var8, var9, var1, var2);
                  if (var34 != null) {
                     return var34;
                  }
               }
            }

            return null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public void playSound(Entity var1, String var2, float var3, float var4) {
      for(int var5 = 0; var5 < this.listeners.size(); ++var5) {
         ((LevelListener)this.listeners.get(var5)).playSound(var2, var1.x, var1.y - (double)var1.heightOffset, var1.z, var3, var4);
      }

   }

   public void playSound(double var1, double var3, double var5, String var7, float var8, float var9) {
      for(int var10 = 0; var10 < this.listeners.size(); ++var10) {
         ((LevelListener)this.listeners.get(var10)).playSound(var7, var1, var3, var5, var8, var9);
      }

   }

   public void playStreamingMusic(String var1, int var2, int var3, int var4) {
      for(int var5 = 0; var5 < this.listeners.size(); ++var5) {
         ((LevelListener)this.listeners.get(var5)).playStreamingMusic(var1, var2, var3, var4);
      }

   }

   public void playMusic(double var1, double var3, double var5, String var7, float var8) {
   }

   public void addParticle(String var1, double var2, double var4, double var6, double var8, double var10, double var12) {
      for(int var14 = 0; var14 < this.listeners.size(); ++var14) {
         ((LevelListener)this.listeners.get(var14)).addParticle(var1, var2, var4, var6, var8, var10, var12);
      }

   }

   public boolean addEntity(Entity var1) {
      int var2 = Mth.floor(var1.x / (double)16.0F);
      int var3 = Mth.floor(var1.z / (double)16.0F);
      boolean var4 = false;
      if (var1 instanceof Player) {
         var4 = true;
      }

      if (!var4 && !this.hasChunk(var2, var3)) {
         return false;
      } else {
         if (var1 instanceof Player) {
            Player var5 = (Player)var1;
            this.players.add(var5);
            System.out.println("Player count: " + this.players.size());
         }

         this.getChunk(var2, var3).addEntity(var1);
         this.entities.add(var1);
         this.entityAdded(var1);
         return true;
      }
   }

   protected void entityAdded(Entity var1) {
      for(int var2 = 0; var2 < this.listeners.size(); ++var2) {
         ((LevelListener)this.listeners.get(var2)).entityAdded(var1);
      }

   }

   protected void entityRemoved(Entity var1) {
      for(int var2 = 0; var2 < this.listeners.size(); ++var2) {
         ((LevelListener)this.listeners.get(var2)).entityRemoved(var1);
      }

   }

   public void removeEntity(Entity var1) {
      if (var1.rider != null) {
         var1.rider.ride((Entity)null);
      }

      if (var1.riding != null) {
         var1.ride((Entity)null);
      }

      var1.remove();
      if (var1 instanceof Player) {
         this.players.remove((Player)var1);
      }

   }

   public void removeEntityImmediately(Entity var1) {
      var1.remove();
      if (var1 instanceof Player) {
         this.players.remove((Player)var1);
      }

      int var2 = var1.xChunk;
      int var3 = var1.zChunk;
      if (var1.inChunk && this.hasChunk(var2, var3)) {
         this.getChunk(var2, var3).removeEntity(var1);
      }

      this.entities.remove(var1);
      this.entityRemoved(var1);
   }

   public void addListener(LevelListener var1) {
      this.listeners.add(var1);
   }

   public void removeListener(LevelListener var1) {
      this.listeners.remove(var1);
   }

   public List<AABB> getCubes(Entity var1, AABB var2) {
      this.boxes.clear();
      int var3 = Mth.floor(var2.x0);
      int var4 = Mth.floor(var2.x1 + (double)1.0F);
      int var5 = Mth.floor(var2.y0);
      int var6 = Mth.floor(var2.y1 + (double)1.0F);
      int var7 = Mth.floor(var2.z0);
      int var8 = Mth.floor(var2.z1 + (double)1.0F);

      for(int var9 = var3; var9 < var4; ++var9) {
         for(int var10 = var7; var10 < var8; ++var10) {
            if (this.hasChunkAt(var9, 64, var10)) {
               for(int var11 = var5 - 1; var11 < var6; ++var11) {
                  Tile var12 = Tile.tiles[this.getTile(var9, var11, var10)];
                  if (var12 != null) {
                     var12.addAABBs(this, var9, var11, var10, var2, this.boxes);
                  }
               }
            }
         }
      }

      double var14 = (double)0.25F;
      List var15 = this.getEntities(var1, var2.grow(var14, var14, var14));

      for(int var16 = 0; var16 < var15.size(); ++var16) {
         AABB var13 = ((Entity)var15.get(var16)).getCollideBox();
         if (var13 != null && var13.intersects(var2)) {
            this.boxes.add(var13);
         }

         var13 = var1.getCollideAgainstBox((Entity)var15.get(var16));
         if (var13 != null && var13.intersects(var2)) {
            this.boxes.add(var13);
         }
      }

      return this.boxes;
   }

   public int getSkyDarken(float var1) {
      float var2 = this.getTimeOfDay(var1);
      float var3 = 1.0F - (Mth.cos(var2 * (float)Math.PI * 2.0F) * 2.0F + 0.5F);
      if (var3 < 0.0F) {
         var3 = 0.0F;
      }

      if (var3 > 1.0F) {
         var3 = 1.0F;
      }

      return (int)(var3 * 11.0F);
   }

   public Vec3 getSkyColor(Entity var1, float var2) {
      float var3 = this.getTimeOfDay(var2);
      float var4 = Mth.cos(var3 * (float)Math.PI * 2.0F) * 2.0F + 0.5F;
      if (var4 < 0.0F) {
         var4 = 0.0F;
      }

      if (var4 > 1.0F) {
         var4 = 1.0F;
      }

      int var5 = Mth.floor(var1.x);
      int var6 = Mth.floor(var1.z);
      float var7 = (float)this.getBiomeSource().getTemperature(var5, var6);
      int var8 = this.getBiomeSource().getBiome(var5, var6).getSkyColor(var7);
      float var9 = (float)(var8 >> 16 & 255) / 255.0F;
      float var10 = (float)(var8 >> 8 & 255) / 255.0F;
      float var11 = (float)(var8 & 255) / 255.0F;
      var9 *= var4;
      var10 *= var4;
      var11 *= var4;
      return Vec3.newTemp((double)var9, (double)var10, (double)var11);
   }

   public float getTimeOfDay(float var1) {
      return this.dimension.getTimeOfDay(this.time, var1);
   }

   public float getSunAngle(float var1) {
      float var2 = this.getTimeOfDay(var1);
      return var2 * (float)Math.PI * 2.0F;
   }

   public Vec3 getCloudColor(float var1) {
      float var2 = this.getTimeOfDay(var1);
      float var3 = Mth.cos(var2 * (float)Math.PI * 2.0F) * 2.0F + 0.5F;
      if (var3 < 0.0F) {
         var3 = 0.0F;
      }

      if (var3 > 1.0F) {
         var3 = 1.0F;
      }

      float var4 = (float)(this.cloudColor >> 16 & 255L) / 255.0F;
      float var5 = (float)(this.cloudColor >> 8 & 255L) / 255.0F;
      float var6 = (float)(this.cloudColor & 255L) / 255.0F;
      var4 *= var3 * 0.9F + 0.1F;
      var5 *= var3 * 0.9F + 0.1F;
      var6 *= var3 * 0.85F + 0.15F;
      return Vec3.newTemp((double)var4, (double)var5, (double)var6);
   }

   public Vec3 getFogColor(float var1) {
      float var2 = this.getTimeOfDay(var1);
      return this.dimension.getFogColor(var2, var1);
   }

   public int getTopSolidBlock(int var1, int var2) {
      LevelChunk var3 = this.getChunkAt(var1, var2);

      int var4;
      for(var4 = 127; this.getMaterial(var1, var4, var2).blocksMotion() && var4 > 0; --var4) {
      }

      var1 &= 15;

      for(int var7 = var2 & 15; var4 > 0; --var4) {
         int var5 = var3.getTile(var1, var4, var7);
         if (var5 != 0 && (Tile.tiles[var5].material.blocksMotion() || Tile.tiles[var5].material.isLiquid())) {
            return var4 + 1;
         }
      }

      return -1;
   }

   public int getLightDepth(int var1, int var2) {
      return this.getChunkAt(var1, var2).getHeightmap(var1 & 15, var2 & 15);
   }

   public float getStarBrightness(float var1) {
      float var2 = this.getTimeOfDay(var1);
      float var3 = 1.0F - (Mth.cos(var2 * (float)Math.PI * 2.0F) * 2.0F + 0.75F);
      if (var3 < 0.0F) {
         var3 = 0.0F;
      }

      if (var3 > 1.0F) {
         var3 = 1.0F;
      }

      return var3 * var3 * 0.5F;
   }

   public void addToTickNextTick(int var1, int var2, int var3, int var4) {
      TickNextTickData var5 = new TickNextTickData(var1, var2, var3, var4);
      byte var6 = 8;
      if (this.instaTick) {
         if (this.hasChunksAt(var5.x - var6, var5.y - var6, var5.z - var6, var5.x + var6, var5.y + var6, var5.z + var6)) {
            int var7 = this.getTile(var5.x, var5.y, var5.z);
            if (var7 == var5.tileId && var7 > 0) {
               Tile.tiles[var7].tick(this, var5.x, var5.y, var5.z, this.random);
            }
         }

      } else {
         if (this.hasChunksAt(var1 - var6, var2 - var6, var3 - var6, var1 + var6, var2 + var6, var3 + var6)) {
            if (var4 > 0) {
               var5.delay((long)Tile.tiles[var4].getTickDelay() + this.time);
            }

            if (!this.tickNextTickSet.contains(var5)) {
               this.tickNextTickSet.add(var5);
               this.tickNextTickList.add(var5);
            }
         }

      }
   }

   public void tickEntities() {
      this.entities.removeAll(this.entitiesToRemove);

      for(int var1 = 0; var1 < this.entitiesToRemove.size(); ++var1) {
         Entity var2 = (Entity)this.entitiesToRemove.get(var1);
         int var3 = var2.xChunk;
         int var4 = var2.zChunk;
         if (var2.inChunk && this.hasChunk(var3, var4)) {
            this.getChunk(var3, var4).removeEntity(var2);
         }
      }

      for(int var5 = 0; var5 < this.entitiesToRemove.size(); ++var5) {
         this.entityRemoved((Entity)this.entitiesToRemove.get(var5));
      }

      this.entitiesToRemove.clear();

      for(int var6 = 0; var6 < this.entities.size(); ++var6) {
         Entity var8 = (Entity)this.entities.get(var6);
         if (var8.riding != null) {
            if (!var8.riding.removed && var8.riding.rider == var8) {
               continue;
            }

            var8.riding.rider = null;
            var8.riding = null;
         }

         if (!var8.removed) {
            this.tick(var8);
         }

         if (var8.removed) {
            int var10 = var8.xChunk;
            int var11 = var8.zChunk;
            if (var8.inChunk && this.hasChunk(var10, var11)) {
               this.getChunk(var10, var11).removeEntity(var8);
            }

            this.entities.remove(var6--);
            this.entityRemoved(var8);
         }
      }

      for(int var7 = 0; var7 < this.tileEntityList.size(); ++var7) {
         TileEntity var9 = (TileEntity)this.tileEntityList.get(var7);
         var9.tick();
      }

   }

   public void tick(Entity var1) {
      this.tick(var1, true);
   }

   public void tick(Entity var1, boolean var2) {
      int var3 = Mth.floor(var1.x);
      int var4 = Mth.floor(var1.z);
      byte var5 = 32;
      if (!var2 || this.hasChunksAt(var3 - var5, 0, var4 - var5, var3 + var5, 128, var4 + var5)) {
         var1.xOld = var1.x;
         var1.yOld = var1.y;
         var1.zOld = var1.z;
         var1.yRotO = var1.yRot;
         var1.xRotO = var1.xRot;
         if (var2 && var1.inChunk) {
            if (var1.riding != null) {
               var1.rideTick();
            } else {
               var1.tick();
            }
         }

         if (Double.isNaN(var1.x) || Double.isInfinite(var1.x)) {
            var1.x = var1.xOld;
         }

         if (Double.isNaN(var1.y) || Double.isInfinite(var1.y)) {
            var1.y = var1.yOld;
         }

         if (Double.isNaN(var1.z) || Double.isInfinite(var1.z)) {
            var1.z = var1.zOld;
         }

         if (Double.isNaN((double)var1.xRot) || Double.isInfinite((double)var1.xRot)) {
            var1.xRot = var1.xRotO;
         }

         if (Double.isNaN((double)var1.yRot) || Double.isInfinite((double)var1.yRot)) {
            var1.yRot = var1.yRotO;
         }

         int var6 = Mth.floor(var1.x / (double)16.0F);
         int var7 = Mth.floor(var1.y / (double)16.0F);
         int var8 = Mth.floor(var1.z / (double)16.0F);
         if (!var1.inChunk || var1.xChunk != var6 || var1.yChunk != var7 || var1.zChunk != var8) {
            if (var1.inChunk && this.hasChunk(var1.xChunk, var1.zChunk)) {
               this.getChunk(var1.xChunk, var1.zChunk).removeEntity(var1, var1.yChunk);
            }

            if (this.hasChunk(var6, var8)) {
               var1.inChunk = true;
               this.getChunk(var6, var8).addEntity(var1);
            } else {
               var1.inChunk = false;
            }
         }

         if (var2 && var1.inChunk && var1.rider != null) {
            if (!var1.rider.removed && var1.rider.riding == var1) {
               this.tick(var1.rider);
            } else {
               var1.rider.riding = null;
               var1.rider = null;
            }
         }

      }
   }

   public boolean isUnobstructed(AABB var1) {
      List var2 = this.getEntities((Entity)null, var1);

      for(int var3 = 0; var3 < var2.size(); ++var3) {
         Entity var4 = (Entity)var2.get(var3);
         if (!var4.removed && var4.blocksBuilding) {
            return false;
         }
      }

      return true;
   }

   public boolean containsAnyLiquid(AABB var1) {
      int var2 = Mth.floor(var1.x0);
      int var3 = Mth.floor(var1.x1 + (double)1.0F);
      int var4 = Mth.floor(var1.y0);
      int var5 = Mth.floor(var1.y1 + (double)1.0F);
      int var6 = Mth.floor(var1.z0);
      int var7 = Mth.floor(var1.z1 + (double)1.0F);
      if (var1.x0 < (double)0.0F) {
         --var2;
      }

      if (var1.y0 < (double)0.0F) {
         --var4;
      }

      if (var1.z0 < (double)0.0F) {
         --var6;
      }

      for(int var8 = var2; var8 < var3; ++var8) {
         for(int var9 = var4; var9 < var5; ++var9) {
            for(int var10 = var6; var10 < var7; ++var10) {
               Tile var11 = Tile.tiles[this.getTile(var8, var9, var10)];
               if (var11 != null && var11.material.isLiquid()) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public boolean containsFireTile(AABB var1) {
      int var2 = Mth.floor(var1.x0);
      int var3 = Mth.floor(var1.x1 + (double)1.0F);
      int var4 = Mth.floor(var1.y0);
      int var5 = Mth.floor(var1.y1 + (double)1.0F);
      int var6 = Mth.floor(var1.z0);
      int var7 = Mth.floor(var1.z1 + (double)1.0F);
      if (this.hasChunksAt(var2, var4, var6, var3, var5, var7)) {
         for(int var8 = var2; var8 < var3; ++var8) {
            for(int var9 = var4; var9 < var5; ++var9) {
               for(int var10 = var6; var10 < var7; ++var10) {
                  int var11 = this.getTile(var8, var9, var10);
                  if (var11 == Tile.fire.id || var11 == Tile.lava.id || var11 == Tile.calmLava.id) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   public boolean checkAndHandleWater(AABB var1, Material var2, Entity var3) {
      int var4 = Mth.floor(var1.x0);
      int var5 = Mth.floor(var1.x1 + (double)1.0F);
      int var6 = Mth.floor(var1.y0);
      int var7 = Mth.floor(var1.y1 + (double)1.0F);
      int var8 = Mth.floor(var1.z0);
      int var9 = Mth.floor(var1.z1 + (double)1.0F);
      if (!this.hasChunksAt(var4, var6, var8, var5, var7, var9)) {
         return false;
      } else {
         boolean var10 = false;
         Vec3 var11 = Vec3.newTemp((double)0.0F, (double)0.0F, (double)0.0F);

         for(int var12 = var4; var12 < var5; ++var12) {
            for(int var13 = var6; var13 < var7; ++var13) {
               for(int var14 = var8; var14 < var9; ++var14) {
                  Tile var15 = Tile.tiles[this.getTile(var12, var13, var14)];
                  if (var15 != null && var15.material == var2) {
                     double var16 = (double)((float)(var13 + 1) - LiquidTile.getHeight(this.getData(var12, var13, var14)));
                     if ((double)var7 >= var16) {
                        var10 = true;
                        var15.handleEntityInside(this, var12, var13, var14, var3, var11);
                     }
                  }
               }
            }
         }

         if (var11.length() > (double)0.0F) {
            var11 = var11.normalize();
            double var19 = 0.004;
            var3.xd += var11.x * var19;
            var3.yd += var11.y * var19;
            var3.zd += var11.z * var19;
         }

         return var10;
      }
   }

   public boolean containsMaterial(AABB var1, Material var2) {
      int var3 = Mth.floor(var1.x0);
      int var4 = Mth.floor(var1.x1 + (double)1.0F);
      int var5 = Mth.floor(var1.y0);
      int var6 = Mth.floor(var1.y1 + (double)1.0F);
      int var7 = Mth.floor(var1.z0);
      int var8 = Mth.floor(var1.z1 + (double)1.0F);

      for(int var9 = var3; var9 < var4; ++var9) {
         for(int var10 = var5; var10 < var6; ++var10) {
            for(int var11 = var7; var11 < var8; ++var11) {
               Tile var12 = Tile.tiles[this.getTile(var9, var10, var11)];
               if (var12 != null && var12.material == var2) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public boolean containsLiquid(AABB var1, Material var2) {
      int var3 = Mth.floor(var1.x0);
      int var4 = Mth.floor(var1.x1 + (double)1.0F);
      int var5 = Mth.floor(var1.y0);
      int var6 = Mth.floor(var1.y1 + (double)1.0F);
      int var7 = Mth.floor(var1.z0);
      int var8 = Mth.floor(var1.z1 + (double)1.0F);

      for(int var9 = var3; var9 < var4; ++var9) {
         for(int var10 = var5; var10 < var6; ++var10) {
            for(int var11 = var7; var11 < var8; ++var11) {
               Tile var12 = Tile.tiles[this.getTile(var9, var10, var11)];
               if (var12 != null && var12.material == var2) {
                  int var13 = this.getData(var9, var10, var11);
                  double var14 = (double)(var10 + 1);
                  if (var13 < 8) {
                     var14 = (double)(var10 + 1) - (double)var13 / (double)8.0F;
                  }

                  if (var14 >= var1.y0) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   public Explosion explode(Entity var1, double var2, double var4, double var6, float var8) {
      return this.explode(var1, var2, var4, var6, var8, false);
   }

   public Explosion explode(Entity var1, double var2, double var4, double var6, float var8, boolean var9) {
      Explosion var10 = new Explosion(this, var1, var2, var4, var6, var8);
      var10.fire = var9;
      var10.explode();
      var10.addParticles();
      return var10;
   }

   public float getSeenPercent(Vec3 var1, AABB var2) {
      double var3 = (double)1.0F / ((var2.x1 - var2.x0) * (double)2.0F + (double)1.0F);
      double var5 = (double)1.0F / ((var2.y1 - var2.y0) * (double)2.0F + (double)1.0F);
      double var7 = (double)1.0F / ((var2.z1 - var2.z0) * (double)2.0F + (double)1.0F);
      int var9 = 0;
      int var10 = 0;

      for(float var11 = 0.0F; var11 <= 1.0F; var11 = (float)((double)var11 + var3)) {
         for(float var12 = 0.0F; var12 <= 1.0F; var12 = (float)((double)var12 + var5)) {
            for(float var13 = 0.0F; var13 <= 1.0F; var13 = (float)((double)var13 + var7)) {
               double var14 = var2.x0 + (var2.x1 - var2.x0) * (double)var11;
               double var16 = var2.y0 + (var2.y1 - var2.y0) * (double)var12;
               double var18 = var2.z0 + (var2.z1 - var2.z0) * (double)var13;
               if (this.clip(Vec3.newTemp(var14, var16, var18), var1) == null) {
                  ++var9;
               }

               ++var10;
            }
         }
      }

      return (float)var9 / (float)var10;
   }

   public void extinguishFire(int var1, int var2, int var3, int var4) {
      if (var4 == 0) {
         --var2;
      }

      if (var4 == 1) {
         ++var2;
      }

      if (var4 == 2) {
         --var3;
      }

      if (var4 == 3) {
         ++var3;
      }

      if (var4 == 4) {
         --var1;
      }

      if (var4 == 5) {
         ++var1;
      }

      if (this.getTile(var1, var2, var3) == Tile.fire.id) {
         this.playSound((double)((float)var1 + 0.5F), (double)((float)var2 + 0.5F), (double)((float)var3 + 0.5F), "random.fizz", 0.5F, 2.6F + (this.random.nextFloat() - this.random.nextFloat()) * 0.8F);
         this.setTile(var1, var2, var3, 0);
      }

   }

   public Entity findSubclassOf(Class<? extends Entity> var1) {
      return null;
   }

   public String gatherStats() {
      return "All: " + this.entities.size();
   }

   public String gatherChunkSourceStats() {
      return this.chunkSource.gatherStats();
   }

   public TileEntity getTileEntity(int var1, int var2, int var3) {
      LevelChunk var4 = this.getChunk(var1 >> 4, var3 >> 4);
      return var4 != null ? var4.getTileEntity(var1 & 15, var2, var3 & 15) : null;
   }

   public void setTileEntity(int var1, int var2, int var3, TileEntity var4) {
      LevelChunk var5 = this.getChunk(var1 >> 4, var3 >> 4);
      if (var5 != null) {
         var5.setTileEntity(var1 & 15, var2, var3 & 15, var4);
      }

   }

   public void removeTileEntity(int var1, int var2, int var3) {
      LevelChunk var4 = this.getChunk(var1 >> 4, var3 >> 4);
      if (var4 != null) {
         var4.removeTileEntity(var1 & 15, var2, var3 & 15);
      }

   }

   public boolean isSolidTile(int var1, int var2, int var3) {
      Tile var4 = Tile.tiles[this.getTile(var1, var2, var3)];
      return var4 == null ? false : var4.isSolidRender();
   }

   public void forceSave(ProgressListener var1) {
      this.save(true, var1);
   }

   public int getLightsToUpdate() {
      return this.lightUpdates.size();
   }

   public boolean updateLights() {
      if (this.maxRecurse >= 50) {
         return false;
      } else {
         ++this.maxRecurse;

         try {
            int var1 = 500;

            while(this.lightUpdates.size() > 0) {
               --var1;
               if (var1 <= 0) {
                  boolean var2 = true;
                  return var2;
               }

               ((LightUpdate)this.lightUpdates.remove(this.lightUpdates.size() - 1)).update(this);
            }

            boolean var6 = false;
            return var6;
         } finally {
            --this.maxRecurse;
         }
      }
   }

   public void updateLight(LightLayer var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      this.updateLight(var1, var2, var3, var4, var5, var6, var7, true);
   }

   public void updateLight(LightLayer var1, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8) {
      if (!this.dimension.hasCeiling || var1 != LightLayer.Sky) {
         ++maxLoop;
         if (maxLoop == 50) {
            --maxLoop;
         } else {
            int var9 = (var5 + var2) / 2;
            int var10 = (var7 + var4) / 2;
            if (!this.hasChunkAt(var9, 64, var10)) {
               --maxLoop;
            } else if (!this.getChunkAt(var9, var10).isEmpty()) {
               int var11 = this.lightUpdates.size();
               if (var8) {
                  int var12 = 5;
                  if (var12 > var11) {
                     var12 = var11;
                  }

                  for(int var13 = 0; var13 < var12; ++var13) {
                     LightUpdate var14 = (LightUpdate)this.lightUpdates.get(this.lightUpdates.size() - var13 - 1);
                     if (var14.layer == var1 && var14.expandToContain(var2, var3, var4, var5, var6, var7)) {
                        --maxLoop;
                        return;
                     }
                  }
               }

               this.lightUpdates.add(new LightUpdate(var1, var2, var3, var4, var5, var6, var7));
               int var15 = 1000000;
               if (this.lightUpdates.size() > 1000000) {
                  System.out.println("More than " + var15 + " updates, aborting lighting updates");
                  this.lightUpdates.clear();
               }

               --maxLoop;
            }
         }
      }
   }

   public void updateSkyBrightness() {
      int var1 = this.getSkyDarken(1.0F);
      if (var1 != this.skyDarken) {
         this.skyDarken = var1;
      }

   }

   public void setSpawnSettings(boolean var1, boolean var2) {
      this.spawnEnemies = var1;
      this.spawnFriendlies = var2;
   }

   public void tick() {
      MobSpawner.tick(this, this.spawnEnemies, this.spawnFriendlies);
      this.chunkSource.tick();
      int var1 = this.getSkyDarken(1.0F);
      if (var1 != this.skyDarken) {
         this.skyDarken = var1;

         for(int var2 = 0; var2 < this.listeners.size(); ++var2) {
            ((LevelListener)this.listeners.get(var2)).skyColorChanged();
         }
      }

      ++this.time;
      if (this.time % (long)this.saveInterval == 0L) {
         this.save(false, (ProgressListener)null);
      }

      this.tickPendingTicks(false);
      this.tickTiles();
   }

   protected void tickTiles() {
      this.chunksToPoll.clear();

      for(int var1 = 0; var1 < this.players.size(); ++var1) {
         Player var2 = (Player)this.players.get(var1);
         int var3 = Mth.floor(var2.x / (double)16.0F);
         int var4 = Mth.floor(var2.z / (double)16.0F);
         byte var5 = 9;

         for(int var6 = -var5; var6 <= var5; ++var6) {
            for(int var7 = -var5; var7 <= var5; ++var7) {
               this.chunksToPoll.add(new ChunkPos(var6 + var3, var7 + var4));
            }
         }
      }

      if (this.delayUntilNextMoodSound > 0) {
         --this.delayUntilNextMoodSound;
      }

      for(ChunkPos var13 : this.chunksToPoll) {
         int var14 = var13.x * 16;
         int var15 = var13.z * 16;
         LevelChunk var16 = this.getChunk(var13.x, var13.z);
         if (this.delayUntilNextMoodSound == 0) {
            this.randValue = this.randValue * 3 + this.addend;
            int var17 = this.randValue >> 2;
            int var19 = var17 & 15;
            int var8 = var17 >> 8 & 15;
            int var9 = var17 >> 16 & 127;
            int var10 = var16.getTile(var19, var9, var8);
            var19 += var14;
            var8 += var15;
            if (var10 == 0 && this.getRawBrightness(var19, var9, var8) <= this.random.nextInt(8) && this.getBrightness(LightLayer.Sky, var19, var9, var8) <= 0) {
               Player var11 = this.getNearestPlayer((double)var19 + (double)0.5F, (double)var9 + (double)0.5F, (double)var8 + (double)0.5F, (double)8.0F);
               if (var11 != null && var11.distanceToSqr((double)var19 + (double)0.5F, (double)var9 + (double)0.5F, (double)var8 + (double)0.5F) > (double)4.0F) {
                  this.playSound((double)var19 + (double)0.5F, (double)var9 + (double)0.5F, (double)var8 + (double)0.5F, "ambient.cave.cave", 0.7F, 0.8F + this.random.nextFloat() * 0.2F);
                  this.delayUntilNextMoodSound = this.random.nextInt(12000) + 6000;
               }
            }
         }

         for(int var18 = 0; var18 < 80; ++var18) {
            this.randValue = this.randValue * 3 + this.addend;
            int var21 = this.randValue >> 2;
            int var23 = var21 & 15;
            int var24 = var21 >> 8 & 15;
            int var25 = var21 >> 16 & 127;
            byte var26 = var16.blocks[var23 << 11 | var24 << 7 | var25];
            if (Tile.shouldTick[var26]) {
               Tile.tiles[var26].tick(this, var23 + var14, var25, var24 + var15, this.random);
            }
         }
      }

   }

   public boolean tickPendingTicks(boolean var1) {
      int var2 = this.tickNextTickList.size();
      if (var2 != this.tickNextTickSet.size()) {
         throw new IllegalStateException("TickNextTick list out of synch");
      } else {
         if (var2 > 1000) {
            var2 = 1000;
         }

         for(int var3 = 0; var3 < var2; ++var3) {
            TickNextTickData var4 = (TickNextTickData)this.tickNextTickList.first();
            if (!var1 && var4.delay > this.time) {
               break;
            }

            this.tickNextTickList.remove(var4);
            this.tickNextTickSet.remove(var4);
            byte var5 = 8;
            if (this.hasChunksAt(var4.x - var5, var4.y - var5, var4.z - var5, var4.x + var5, var4.y + var5, var4.z + var5)) {
               int var6 = this.getTile(var4.x, var4.y, var4.z);
               if (var6 == var4.tileId && var6 > 0) {
                  Tile.tiles[var6].tick(this, var4.x, var4.y, var4.z, this.random);
               }
            }
         }

         return this.tickNextTickList.size() != 0;
      }
   }

   public void animateTick(int var1, int var2, int var3) {
      byte var4 = 16;
      Random var5 = new Random();

      for(int var6 = 0; var6 < 1000; ++var6) {
         int var7 = var1 + this.random.nextInt(var4) - this.random.nextInt(var4);
         int var8 = var2 + this.random.nextInt(var4) - this.random.nextInt(var4);
         int var9 = var3 + this.random.nextInt(var4) - this.random.nextInt(var4);
         int var10 = this.getTile(var7, var8, var9);
         if (var10 > 0) {
            Tile.tiles[var10].animateTick(this, var7, var8, var9, var5);
         }
      }

   }

   public List<Entity> getEntities(Entity var1, AABB var2) {
      this.es.clear();
      int var3 = Mth.floor((var2.x0 - (double)2.0F) / (double)16.0F);
      int var4 = Mth.floor((var2.x1 + (double)2.0F) / (double)16.0F);
      int var5 = Mth.floor((var2.z0 - (double)2.0F) / (double)16.0F);
      int var6 = Mth.floor((var2.z1 + (double)2.0F) / (double)16.0F);

      for(int var7 = var3; var7 <= var4; ++var7) {
         for(int var8 = var5; var8 <= var6; ++var8) {
            if (this.hasChunk(var7, var8)) {
               this.getChunk(var7, var8).getEntities(var1, var2, this.es);
            }
         }
      }

      return this.es;
   }

   public List<Entity> getEntitiesOfClass(Class<? extends Entity> var1, AABB var2) {
      int var3 = Mth.floor((var2.x0 - (double)2.0F) / (double)16.0F);
      int var4 = Mth.floor((var2.x1 + (double)2.0F) / (double)16.0F);
      int var5 = Mth.floor((var2.z0 - (double)2.0F) / (double)16.0F);
      int var6 = Mth.floor((var2.z1 + (double)2.0F) / (double)16.0F);
      ArrayList var7 = new ArrayList();

      for(int var8 = var3; var8 <= var4; ++var8) {
         for(int var9 = var5; var9 <= var6; ++var9) {
            if (this.hasChunk(var8, var9)) {
               this.getChunk(var8, var9).getEntitiesOfClass(var1, var2, var7);
            }
         }
      }

      return var7;
   }

   public List<Entity> getAllEntities() {
      return this.entities;
   }

   public void tileEntityChanged(int var1, int var2, int var3, TileEntity var4) {
      if (this.hasChunkAt(var1, var2, var3)) {
         this.getChunkAt(var1, var3).markUnsaved();
      }

      for(int var5 = 0; var5 < this.listeners.size(); ++var5) {
         ((LevelListener)this.listeners.get(var5)).tileEntityChanged(var1, var2, var3, var4);
      }

   }

   public int countInstanceOf(Class<?> var1) {
      int var2 = 0;

      for(int var3 = 0; var3 < this.entities.size(); ++var3) {
         Entity var4 = (Entity)this.entities.get(var3);
         if (var1.isAssignableFrom(var4.getClass())) {
            ++var2;
         }
      }

      return var2;
   }

   public void addEntities(List<Entity> var1) {
      this.entities.addAll(var1);

      for(int var2 = 0; var2 < var1.size(); ++var2) {
         this.entityAdded((Entity)var1.get(var2));
      }

   }

   public void removeEntities(List<Entity> var1) {
      this.entitiesToRemove.addAll(var1);
   }

   public void prepare() {
      while(this.chunkSource.tick()) {
      }

   }

   public boolean mayPlace(int var1, int var2, int var3, int var4, boolean var5) {
      int var6 = this.getTile(var2, var3, var4);
      Tile var7 = Tile.tiles[var6];
      Tile var8 = Tile.tiles[var1];
      AABB var9 = var8.getAABB(this, var2, var3, var4);
      if (var5) {
         var9 = null;
      }

      if (var9 != null && !this.isUnobstructed(var9)) {
         return false;
      } else if (var7 != Tile.water && var7 != Tile.calmWater && var7 != Tile.lava && var7 != Tile.calmLava && var7 != Tile.fire && var7 != Tile.topSnow) {
         return var1 > 0 && var7 == null && var8.mayPlace(this, var2, var3, var4);
      } else {
         return true;
      }
   }

   public int getSeaLevel() {
      return 63;
   }

   public Path findPath(Entity var1, Entity var2, float var3) {
      int var4 = Mth.floor(var1.x);
      int var5 = Mth.floor(var1.y);
      int var6 = Mth.floor(var1.z);
      int var7 = (int)(var3 + 16.0F);
      int var8 = var4 - var7;
      int var9 = var5 - var7;
      int var10 = var6 - var7;
      int var11 = var4 + var7;
      int var12 = var5 + var7;
      int var13 = var6 + var7;
      Region var14 = new Region(this, var8, var9, var10, var11, var12, var13);
      return (new PathFinder(var14)).findPath(var1, var2, var3);
   }

   public Path findPath(Entity var1, int var2, int var3, int var4, float var5) {
      int var6 = Mth.floor(var1.x);
      int var7 = Mth.floor(var1.y);
      int var8 = Mth.floor(var1.z);
      int var9 = (int)(var5 + 8.0F);
      int var10 = var6 - var9;
      int var11 = var7 - var9;
      int var12 = var8 - var9;
      int var13 = var6 + var9;
      int var14 = var7 + var9;
      int var15 = var8 + var9;
      Region var16 = new Region(this, var10, var11, var12, var13, var14, var15);
      return (new PathFinder(var16)).findPath(var1, var2, var3, var4, var5);
   }

   public boolean getDirectSignal(int var1, int var2, int var3, int var4) {
      int var5 = this.getTile(var1, var2, var3);
      return var5 == 0 ? false : Tile.tiles[var5].getDirectSignal(this, var1, var2, var3, var4);
   }

   public boolean hasDirectSignal(int var1, int var2, int var3) {
      if (this.getDirectSignal(var1, var2 - 1, var3, 0)) {
         return true;
      } else if (this.getDirectSignal(var1, var2 + 1, var3, 1)) {
         return true;
      } else if (this.getDirectSignal(var1, var2, var3 - 1, 2)) {
         return true;
      } else if (this.getDirectSignal(var1, var2, var3 + 1, 3)) {
         return true;
      } else if (this.getDirectSignal(var1 - 1, var2, var3, 4)) {
         return true;
      } else {
         return this.getDirectSignal(var1 + 1, var2, var3, 5);
      }
   }

   public boolean getSignal(int var1, int var2, int var3, int var4) {
      if (this.isSolidTile(var1, var2, var3)) {
         return this.hasDirectSignal(var1, var2, var3);
      } else {
         int var5 = this.getTile(var1, var2, var3);
         return var5 == 0 ? false : Tile.tiles[var5].getSignal(this, var1, var2, var3, var4);
      }
   }

   public boolean hasNeighborSignal(int var1, int var2, int var3) {
      if (this.getSignal(var1, var2 - 1, var3, 0)) {
         return true;
      } else if (this.getSignal(var1, var2 + 1, var3, 1)) {
         return true;
      } else if (this.getSignal(var1, var2, var3 - 1, 2)) {
         return true;
      } else if (this.getSignal(var1, var2, var3 + 1, 3)) {
         return true;
      } else if (this.getSignal(var1 - 1, var2, var3, 4)) {
         return true;
      } else {
         return this.getSignal(var1 + 1, var2, var3, 5);
      }
   }

   public Player getNearestPlayer(Entity var1, double var2) {
      return this.getNearestPlayer(var1.x, var1.y, var1.z, var2);
   }

   public Player getNearestPlayer(double var1, double var3, double var5, double var7) {
      double var9 = (double)-1.0F;
      Player var11 = null;

      for(int var12 = 0; var12 < this.players.size(); ++var12) {
         Player var13 = (Player)this.players.get(var12);
         double var14 = var13.distanceToSqr(var1, var3, var5);
         if ((var7 < (double)0.0F || var14 < var7 * var7) && (var9 == (double)-1.0F || var14 < var9)) {
            var9 = var14;
            var11 = var13;
         }
      }

      return var11;
   }

   public byte[] getBlocksAndData(int var1, int var2, int var3, int var4, int var5, int var6) {
      byte[] var7 = new byte[var4 * var5 * var6 * 5 / 2];
      int var8 = var1 >> 4;
      int var9 = var3 >> 4;
      int var10 = var1 + var4 - 1 >> 4;
      int var11 = var3 + var6 - 1 >> 4;
      int var12 = 0;
      int var13 = var2;
      int var14 = var2 + var5;
      if (var2 < 0) {
         var13 = 0;
      }

      if (var14 > 128) {
         var14 = 128;
      }

      for(int var15 = var8; var15 <= var10; ++var15) {
         int var16 = var1 - var15 * 16;
         int var17 = var1 + var4 - var15 * 16;
         if (var16 < 0) {
            var16 = 0;
         }

         if (var17 > 16) {
            var17 = 16;
         }

         for(int var18 = var9; var18 <= var11; ++var18) {
            int var19 = var3 - var18 * 16;
            int var20 = var3 + var6 - var18 * 16;
            if (var19 < 0) {
               var19 = 0;
            }

            if (var20 > 16) {
               var20 = 16;
            }

            var12 = this.getChunk(var15, var18).getBlocksAndData(var7, var16, var13, var19, var17, var14, var20, var12);
         }
      }

      return var7;
   }

   public void setBlocksAndData(int var1, int var2, int var3, int var4, int var5, int var6, byte[] var7) {
      int var8 = var1 >> 4;
      int var9 = var3 >> 4;
      int var10 = var1 + var4 - 1 >> 4;
      int var11 = var3 + var6 - 1 >> 4;
      int var12 = 0;
      int var13 = var2;
      int var14 = var2 + var5;
      if (var2 < 0) {
         var13 = 0;
      }

      if (var14 > 128) {
         var14 = 128;
      }

      for(int var15 = var8; var15 <= var10; ++var15) {
         int var16 = var1 - var15 * 16;
         int var17 = var1 + var4 - var15 * 16;
         if (var16 < 0) {
            var16 = 0;
         }

         if (var17 > 16) {
            var17 = 16;
         }

         for(int var18 = var9; var18 <= var11; ++var18) {
            int var19 = var3 - var18 * 16;
            int var20 = var3 + var6 - var18 * 16;
            if (var19 < 0) {
               var19 = 0;
            }

            if (var20 > 16) {
               var20 = 16;
            }

            var12 = this.getChunk(var15, var18).setBlocksAndData(var7, var16, var13, var19, var17, var14, var20, var12);
            this.setTilesDirty(var15 * 16 + var16, var13, var18 * 16 + var19, var15 * 16 + var17, var14, var18 * 16 + var20);
         }
      }

   }

   public void disconnect() {
   }

   public void checkSession() {
      try {
         File var1 = new File(this.dir, "session.lock");
         DataInputStream var2 = new DataInputStream(new FileInputStream(var1));

         try {
            if (var2.readLong() != this.sessionId) {
               throw new LevelConflictException("The save is being accessed from another location, aborting");
            }
         } finally {
            var2.close();
         }

      } catch (IOException var7) {
         throw new LevelConflictException("Failed to check session lock, aborting");
      }
   }

   public void setTime(long var1) {
      this.time = var1;
   }

   public void ensureAdded(Entity var1) {
      int var2 = Mth.floor(var1.x / (double)16.0F);
      int var3 = Mth.floor(var1.z / (double)16.0F);
      byte var4 = 2;

      for(int var5 = var2 - var4; var5 <= var2 + var4; ++var5) {
         for(int var6 = var3 - var4; var6 <= var3 + var4; ++var6) {
            this.getChunk(var5, var6);
         }
      }

      if (!this.entities.contains(var1)) {
         this.entities.add(var1);
      }

   }

   public boolean mayInteract(Player var1, int var2, int var3, int var4) {
      return true;
   }

   public void broadcastEntityEvent(Entity var1, byte var2) {
   }

   public void removeAllPendingEntityRemovals() {
      this.entities.removeAll(this.entitiesToRemove);

      for(int var1 = 0; var1 < this.entitiesToRemove.size(); ++var1) {
         Entity var2 = (Entity)this.entitiesToRemove.get(var1);
         int var3 = var2.xChunk;
         int var4 = var2.zChunk;
         if (var2.inChunk && this.hasChunk(var3, var4)) {
            this.getChunk(var3, var4).removeEntity(var2);
         }
      }

      for(int var5 = 0; var5 < this.entitiesToRemove.size(); ++var5) {
         this.entityRemoved((Entity)this.entitiesToRemove.get(var5));
      }

      this.entitiesToRemove.clear();

      for(int var6 = 0; var6 < this.entities.size(); ++var6) {
         Entity var7 = (Entity)this.entities.get(var6);
         if (var7.riding != null) {
            if (!var7.riding.removed && var7.riding.rider == var7) {
               continue;
            }

            var7.riding.rider = null;
            var7.riding = null;
         }

         if (var7.removed) {
            int var8 = var7.xChunk;
            int var9 = var7.zChunk;
            if (var7.inChunk && this.hasChunk(var8, var9)) {
               this.getChunk(var8, var9).removeEntity(var7);
            }

            this.entities.remove(var6--);
            this.entityRemoved(var7);
         }
      }

   }

   public ChunkSource getChunkSource() {
      return this.chunkSource;
   }

   public void tileEvent(int var1, int var2, int var3, int var4, int var5) {
      int var6 = this.getTile(var1, var2, var3);
      if (var6 > 0) {
         Tile.tiles[var6].triggerEvent(this, var1, var2, var3, var4, var5);
      }

   }
}
