package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.world.entity.Entity;
import util.Mth;

public class AddEntityPacket extends Packet {
   public static final int BOAT = 1;
   public static final int MINECART_RIDEABLE = 10;
   public static final int MINECART_CHEST = 11;
   public static final int MINECART_FURNACE = 12;
   public static final int PRIMED_TNT = 50;
   public static final int ARROW = 60;
   public static final int SNOWBALL = 61;
   public static final int EGG = 62;
   public static final int FISH_HOOK = 90;
   public static final int FALLING_SAND = 70;
   public static final int FALLING_GRAVEL = 71;
   public int id;
   public int x;
   public int y;
   public int z;
   public int type;

   public AddEntityPacket() {
   }

   public AddEntityPacket(Entity var1, int var2) {
      this.id = var1.entityId;
      this.x = Mth.floor(var1.x * (double)32.0F);
      this.y = Mth.floor(var1.y * (double)32.0F);
      this.z = Mth.floor(var1.z * (double)32.0F);
      this.type = var2;
   }

   public void read(DataInputStream var1) throws IOException {
      this.id = var1.readInt();
      this.type = var1.readByte();
      this.x = var1.readInt();
      this.y = var1.readInt();
      this.z = var1.readInt();
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeInt(this.id);
      var1.writeByte(this.type);
      var1.writeInt(this.x);
      var1.writeInt(this.y);
      var1.writeInt(this.z);
   }

   public void handle(PacketListener var1) {
      var1.handleAddEntity(this);
   }

   public int getEstimatedSize() {
      return 17;
   }
}
