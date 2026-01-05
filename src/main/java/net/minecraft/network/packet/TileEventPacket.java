package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TileEventPacket extends Packet {
   public int x;
   public int y;
   public int z;
   public int b0;
   public int b1;

   public TileEventPacket() {
   }

   public TileEventPacket(int var1, int var2, int var3, int var4, int var5) {
      this.x = var1;
      this.y = var2;
      this.z = var3;
      this.b0 = var4;
      this.b1 = var5;
   }

   public void read(DataInputStream var1) throws IOException {
      this.x = var1.readInt();
      this.y = var1.readShort();
      this.z = var1.readInt();
      this.b0 = var1.read();
      this.b1 = var1.read();
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeInt(this.x);
      var1.writeShort(this.y);
      var1.writeInt(this.z);
      var1.write(this.b0);
      var1.write(this.b1);
   }

   public void handle(PacketListener var1) {
      var1.handleTileEvent(this);
   }

   public int getEstimatedSize() {
      return 12;
   }
}
