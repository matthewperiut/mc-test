package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.world.level.TilePos;

public class ExplodePacket extends Packet {
   public double x;
   public double y;
   public double z;
   public float r;
   public Set<TilePos> toBlow;

   public ExplodePacket() {
   }

   public ExplodePacket(double var1, double var3, double var5, float var7, Set<TilePos> var8) {
      this.x = var1;
      this.y = var3;
      this.z = var5;
      this.r = var7;
      this.toBlow = new HashSet(var8);
   }

   public void read(DataInputStream var1) throws IOException {
      this.x = var1.readDouble();
      this.y = var1.readDouble();
      this.z = var1.readDouble();
      this.r = var1.readFloat();
      int var2 = var1.readInt();
      this.toBlow = new HashSet();
      int var3 = (int)this.x;
      int var4 = (int)this.y;
      int var5 = (int)this.z;

      for(int var6 = 0; var6 < var2; ++var6) {
         int var7 = var1.readByte() + var3;
         int var8 = var1.readByte() + var4;
         int var9 = var1.readByte() + var5;
         this.toBlow.add(new TilePos(var7, var8, var9));
      }

   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeDouble(this.x);
      var1.writeDouble(this.y);
      var1.writeDouble(this.z);
      var1.writeFloat(this.r);
      var1.writeInt(this.toBlow.size());
      int var2 = (int)this.x;
      int var3 = (int)this.y;
      int var4 = (int)this.z;

      for(TilePos var6 : this.toBlow) {
         int var7 = var6.x - var2;
         int var8 = var6.y - var3;
         int var9 = var6.z - var4;
         var1.writeByte(var7);
         var1.writeByte(var8);
         var1.writeByte(var9);
      }

   }

   public void handle(PacketListener var1) {
      var1.handleExplosion(this);
   }

   public int getEstimatedSize() {
      return 32 + this.toBlow.size() * 3;
   }
}
