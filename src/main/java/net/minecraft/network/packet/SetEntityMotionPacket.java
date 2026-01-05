package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.world.entity.Entity;

public class SetEntityMotionPacket extends Packet {
   public int id;
   public int xa;
   public int ya;
   public int za;

   public SetEntityMotionPacket() {
   }

   public SetEntityMotionPacket(Entity var1) {
      this(var1.entityId, var1.xd, var1.yd, var1.zd);
   }

   public SetEntityMotionPacket(int var1, double var2, double var4, double var6) {
      this.id = var1;
      double var8 = 3.9;
      if (var2 < -var8) {
         var2 = -var8;
      }

      if (var4 < -var8) {
         var4 = -var8;
      }

      if (var6 < -var8) {
         var6 = -var8;
      }

      if (var2 > var8) {
         var2 = var8;
      }

      if (var4 > var8) {
         var4 = var8;
      }

      if (var6 > var8) {
         var6 = var8;
      }

      this.xa = (int)(var2 * (double)8000.0F);
      this.ya = (int)(var4 * (double)8000.0F);
      this.za = (int)(var6 * (double)8000.0F);
   }

   public void read(DataInputStream var1) throws IOException {
      this.id = var1.readInt();
      this.xa = var1.readShort();
      this.ya = var1.readShort();
      this.za = var1.readShort();
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeInt(this.id);
      var1.writeShort(this.xa);
      var1.writeShort(this.ya);
      var1.writeShort(this.za);
   }

   public void handle(PacketListener var1) {
      var1.handleSetEntityMotion(this);
   }

   public int getEstimatedSize() {
      return 10;
   }
}
