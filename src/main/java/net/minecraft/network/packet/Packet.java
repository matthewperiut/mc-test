package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class Packet {
   private static Map<Integer, Class<? extends Packet>> idToClassMap = new HashMap();
   private static Map<Class<? extends Packet>, Integer> classToIdMap = new HashMap();
   public final long createTime = System.currentTimeMillis();
   public boolean shouldDelay = false;

   public Packet() {
   }

   static void map(int var0, Class<? extends Packet> var1) {
      if (idToClassMap.containsKey(var0)) {
         throw new IllegalArgumentException("Duplicate packet id:" + var0);
      } else if (classToIdMap.containsKey(var1)) {
         throw new IllegalArgumentException("Duplicate packet class:" + var1);
      } else {
         idToClassMap.put(var0, var1);
         classToIdMap.put(var1, var0);
      }
   }

   public static Packet getPacket(int var0) {
      try {
         Class var1 = (Class)idToClassMap.get(var0);
         return var1 == null ? null : (Packet)var1.newInstance();
      } catch (Exception var2) {
         var2.printStackTrace();
         System.out.println("Skipping packet with id " + var0);
         return null;
      }
   }

   public final int getId() {
      return (Integer)classToIdMap.get(this.getClass());
   }

   public static Packet readPacket(DataInputStream var0) throws IOException {
      int var1 = var0.read();
      if (var1 == -1) {
         return null;
      } else {
         Packet var2 = getPacket(var1);
         if (var2 == null) {
            throw new IOException("Bad packet id " + var1);
         } else {
            var2.read(var0);
            return var2;
         }
      }
   }

   public static void writePacket(Packet var0, DataOutputStream var1) throws IOException {
      var1.write(var0.getId());
      var0.write(var1);
   }

   public abstract void read(DataInputStream var1) throws IOException;

   public abstract void write(DataOutputStream var1) throws IOException;

   public abstract void handle(PacketListener var1);

   public abstract int getEstimatedSize();

   static {
      map(0, KeepAlivePacket.class);
      map(1, LoginPacket.class);
      map(2, PreLoginPacket.class);
      map(3, ChatPacket.class);
      map(4, SetTimePacket.class);
      map(5, SetEquippedItemPacket.class);
      map(6, SetSpawnPositionPacket.class);
      map(7, InteractPacket.class);
      map(8, SetHealthPacket.class);
      map(9, RespawnPacket.class);
      map(10, MovePlayerPacket.class);
      map(11, MovePlayerPacket.Pos.class);
      map(12, MovePlayerPacket.Rot.class);
      map(13, MovePlayerPacket.PosRot.class);
      map(14, PlayerActionPacket.class);
      map(15, UseItemPacket.class);
      map(16, SetCarriedItemPacket.class);
      map(18, AnimatePacket.class);
      map(19, PlayerCommandPacket.class);
      map(20, AddPlayerPacket.class);
      map(21, AddItemEntityPacket.class);
      map(22, TakeItemEntityPacket.class);
      map(23, AddEntityPacket.class);
      map(24, AddMobPacket.class);
      map(25, AddPaintingPacket.class);
      map(28, SetEntityMotionPacket.class);
      map(29, RemoveEntityPacket.class);
      map(30, MoveEntityPacket.class);
      map(31, MoveEntityPacket.Pos.class);
      map(32, MoveEntityPacket.Rot.class);
      map(33, MoveEntityPacket.PosRot.class);
      map(34, TeleportEntityPacket.class);
      map(38, EntityEventPacket.class);
      map(39, SetRidingPacket.class);
      map(40, SetEntityDataPacket.class);
      map(50, ChunkVisibilityPacket.class);
      map(51, BlockRegionUpdatePacket.class);
      map(52, ChunkTilesUpdatePacket.class);
      map(53, TileUpdatePacket.class);
      map(54, TileEventPacket.class);
      map(60, ExplodePacket.class);
      map(100, ContainerOpenPacket.class);
      map(101, ContainerClosePacket.class);
      map(102, ContainerClickPacket.class);
      map(103, ContainerSetSlotPacket.class);
      map(104, ContainerSetContentPacket.class);
      map(105, ContainerSetDataPacket.class);
      map(106, ContainerAckPacket.class);
      map(130, SignUpdatePacket.class);
      map(255, DisconnectPacket.class);
   }
}
