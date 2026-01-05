package net.minecraft.world.level.tile.entity;

import com.mojang.nbt.CompoundTag;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.packet.Packet;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;

public class TileEntity {
   private static Map<String, Class<? extends TileEntity>> idClassMap = new HashMap();
   private static Map<Class<? extends TileEntity>, String> classIdMap = new HashMap();
   public Level level;
   public int x;
   public int y;
   public int z;

   public TileEntity() {
   }

   private static void setId(Class<? extends TileEntity> var0, String var1) {
      if (classIdMap.containsKey(var1)) {
         throw new IllegalArgumentException("Duplicate id: " + var1);
      } else {
         idClassMap.put(var1, var0);
         classIdMap.put(var0, var1);
      }
   }

   public void load(CompoundTag var1) {
      this.x = var1.getInt("x");
      this.y = var1.getInt("y");
      this.z = var1.getInt("z");
   }

   public void save(CompoundTag var1) {
      String var2 = (String)classIdMap.get(this.getClass());
      if (var2 == null) {
         throw new RuntimeException(this.getClass() + " is missing a mapping! This is a bug!");
      } else {
         var1.putString("id", var2);
         var1.putInt("x", this.x);
         var1.putInt("y", this.y);
         var1.putInt("z", this.z);
      }
   }

   public void tick() {
   }

   public static TileEntity loadStatic(CompoundTag var0) {
      TileEntity var1 = null;

      try {
         Class var2 = (Class)idClassMap.get(var0.getString("id"));
         if (var2 != null) {
            var1 = (TileEntity)var2.newInstance();
         }
      } catch (Exception var3) {
         var3.printStackTrace();
      }

      if (var1 != null) {
         var1.load(var0);
      } else {
         System.out.println("Skipping TileEntity with id " + var0.getString("id"));
      }

      return var1;
   }

   public int getData() {
      return this.level.getData(this.x, this.y, this.z);
   }

   public void setData(int var1) {
      this.level.setData(this.x, this.y, this.z, var1);
   }

   public void setChanged() {
      if (this.level != null) {
         this.level.tileEntityChanged(this.x, this.y, this.z, this);
      }

   }

   public double distanceToSqr(double var1, double var3, double var5) {
      double var7 = (double)this.x + (double)0.5F - var1;
      double var9 = (double)this.y + (double)0.5F - var3;
      double var11 = (double)this.z + (double)0.5F - var5;
      return var7 * var7 + var9 * var9 + var11 * var11;
   }

   public Tile getTile() {
      return Tile.tiles[this.level.getTile(this.x, this.y, this.z)];
   }

   public Packet getUpdatePacket() {
      return null;
   }

   static {
      setId(FurnaceTileEntity.class, "Furnace");
      setId(ChestTileEntity.class, "Chest");
      setId(DispenserTileEntity.class, "Trap");
      setId(SignTileEntity.class, "Sign");
      setId(MobSpawnerTileEntity.class, "MobSpawner");
      setId(MusicTileEntity.class, "Music");
   }
}
