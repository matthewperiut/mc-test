package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.world.entity.Entity;
import util.Mth;

public class TeleportEntityPacket extends Packet {
   public int id;
   public int x;
   public int y;
   public int z;
   public byte yRot;
   public byte xRot;

   public TeleportEntityPacket() {
   }

   public TeleportEntityPacket(Entity var1) {
      this.id = var1.entityId;
      this.x = Mth.floor(var1.x * (double)32.0F);
      this.y = Mth.floor(var1.y * (double)32.0F);
      this.z = Mth.floor(var1.z * (double)32.0F);
      this.yRot = (byte)((int)(var1.yRot * 256.0F / 360.0F));
      this.xRot = (byte)((int)(var1.xRot * 256.0F / 360.0F));
   }

   public TeleportEntityPacket(int var1, int var2, int var3, int var4, byte var5, byte var6) {
      this.id = var1;
      this.x = var2;
      this.y = var3;
      this.z = var4;
      this.yRot = var5;
      this.xRot = var6;
   }

   public void read(DataInputStream var1) throws IOException {
      this.id = var1.readInt();
      this.x = var1.readInt();
      this.y = var1.readInt();
      this.z = var1.readInt();
      this.yRot = (byte)var1.read();
      this.xRot = (byte)var1.read();
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeInt(this.id);
      var1.writeInt(this.x);
      var1.writeInt(this.y);
      var1.writeInt(this.z);
      var1.write(this.yRot);
      var1.write(this.xRot);
   }

   public void handle(PacketListener var1) {
      var1.handleTeleportEntity(this);
   }

   public int getEstimatedSize() {
      return 34;
   }
}
