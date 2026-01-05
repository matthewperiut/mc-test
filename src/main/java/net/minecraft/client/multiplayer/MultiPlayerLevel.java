package net.minecraft.client.multiplayer;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import net.minecraft.network.packet.DisconnectPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelListener;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.dimension.Dimension;
import util.IntHashMap;

public class MultiPlayerLevel extends Level {
   private static final int TICKS_BEFORE_RESET = 80;
   private LinkedList<ResetInfo> updatesToReset = new LinkedList();
   private ClientConnection connection;
   private MultiPlayerChunkCache chunkCache;
   private IntHashMap<Entity> entitiesById = new IntHashMap<Entity>();
   private Set<Entity> forced = new HashSet();
   private Set<Entity> reEntries = new HashSet();

   public MultiPlayerLevel(ClientConnection connection, long seed, int dimension) {
      super("MpServer", Dimension.getNew(dimension), seed);
      this.connection = connection;
      this.xSpawn = 8;
      this.ySpawn = 64;
      this.zSpawn = 8;
   }

   public void tick() {
      ++this.time;
      int newDark = this.getSkyDarken(1.0F);
      if (newDark != this.skyDarken) {
         this.skyDarken = newDark;

         for(int i = 0; i < this.listeners.size(); ++i) {
            ((LevelListener)this.listeners.get(i)).skyColorChanged();
         }
      }

      for(int i = 0; i < 10 && !this.reEntries.isEmpty(); ++i) {
         Entity e = (Entity)this.reEntries.iterator().next();
         if (!this.entities.contains(e)) {
            this.addEntity(e);
         }
      }

      this.connection.tick();

      for(int i = 0; i < this.updatesToReset.size(); ++i) {
         ResetInfo r = (ResetInfo)this.updatesToReset.get(i);
         if (--r.ticks == 0) {
            super.setTileAndDataNoUpdate(r.x, r.y, r.z, r.tile, r.data);
            super.sendTileUpdated(r.x, r.y, r.z);
            this.updatesToReset.remove(i--);
         }
      }

   }

   public void clearResetRegion(int x0, int y0, int z0, int x1, int y1, int z1) {
      for(int i = 0; i < this.updatesToReset.size(); ++i) {
         ResetInfo r = (ResetInfo)this.updatesToReset.get(i);
         if (r.x >= x0 && r.y >= y0 && r.z >= z0 && r.x <= x1 && r.y <= y1 && r.z <= z1) {
            this.updatesToReset.remove(i--);
         }
      }

   }

   protected ChunkSource createChunkSource(File dir) {
      this.chunkCache = new MultiPlayerChunkCache(this);
      return this.chunkCache;
   }

   public void validateSpawn() {
      this.xSpawn = 8;
      this.ySpawn = 64;
      this.zSpawn = 8;
   }

   protected void tickTiles() {
   }

   public void addToTickNextTick(int x, int y, int z, int tileId) {
   }

   public boolean tickPendingTicks(boolean force) {
      return false;
   }

   public void setChunkVisible(int x, int z, boolean visible) {
      if (visible) {
         this.chunkCache.create(x, z);
      } else {
         this.chunkCache.drop(x, z);
      }

      if (!visible) {
         this.setTilesDirty(x * 16, 0, z * 16, x * 16 + 15, 128, z * 16 + 15);
      }

   }

   public boolean addEntity(Entity e) {
      boolean ok = super.addEntity(e);
      this.forced.add(e);
      if (!ok) {
         this.reEntries.add(e);
      }

      return ok;
   }

   public void removeEntity(Entity e) {
      super.removeEntity(e);
      this.forced.remove(e);
   }

   protected void entityAdded(Entity e) {
      super.entityAdded(e);
      if (this.reEntries.contains(e)) {
         this.reEntries.remove(e);
      }

   }

   protected void entityRemoved(Entity e) {
      super.entityRemoved(e);
      if (this.forced.contains(e)) {
         this.reEntries.add(e);
      }

   }

   public void putEntity(int id, Entity e) {
      Entity old = this.getEntity(id);
      if (old != null) {
         this.removeEntity(old);
      }

      this.forced.add(e);
      e.entityId = id;
      if (!this.addEntity(e)) {
         this.reEntries.add(e);
      }

      this.entitiesById.put(id, e);
   }

   public Entity getEntity(int id) {
      return this.entitiesById.get(id);
   }

   public Entity removeEntity(int id) {
      Entity e = this.entitiesById.remove(id);
      if (e != null) {
         this.forced.remove(e);
         this.removeEntity(e);
      }

      return e;
   }

   public boolean setDataNoUpdate(int x, int y, int z, int data) {
      int t = this.getTile(x, y, z);
      int d = this.getData(x, y, z);
      if (super.setDataNoUpdate(x, y, z, data)) {
         this.updatesToReset.add(new ResetInfo(x, y, z, t, d));
         return true;
      } else {
         return false;
      }
   }

   public boolean setTileAndDataNoUpdate(int x, int y, int z, int tile, int data) {
      int t = this.getTile(x, y, z);
      int d = this.getData(x, y, z);
      if (super.setTileAndDataNoUpdate(x, y, z, tile, data)) {
         this.updatesToReset.add(new ResetInfo(x, y, z, t, d));
         return true;
      } else {
         return false;
      }
   }

   public boolean setTileNoUpdate(int x, int y, int z, int tile) {
      int t = this.getTile(x, y, z);
      int d = this.getData(x, y, z);
      if (super.setTileNoUpdate(x, y, z, tile)) {
         this.updatesToReset.add(new ResetInfo(x, y, z, t, d));
         return true;
      } else {
         return false;
      }
   }

   public boolean doSetTileAndData(int x, int y, int z, int tile, int data) {
      this.clearResetRegion(x, y, z, x, y, z);
      if (super.setTileAndDataNoUpdate(x, y, z, tile, data)) {
         this.tileUpdated(x, y, z, tile);
         return true;
      } else {
         return false;
      }
   }

   public void disconnect() {
      this.connection.send(new DisconnectPacket("Quitting"));
   }

   private class ResetInfo {
      int x;
      int y;
      int z;
      int ticks;
      int tile;
      int data;

      public ResetInfo(int x, int y, int z, int tile, int data) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.ticks = 80;
         this.tile = tile;
         this.data = data;
      }
   }
}
