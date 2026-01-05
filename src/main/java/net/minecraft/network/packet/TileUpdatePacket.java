package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.world.level.Level;

public class TileUpdatePacket extends Packet {
   public int x;
   public int y;
   public int z;
   public int block;
   public int data;

   public TileUpdatePacket() {
      this.shouldDelay = true;
   }

   public TileUpdatePacket(int var1, int var2, int var3, Level var4) {
      this.shouldDelay = true;
      this.x = var1;
      this.y = var2;
      this.z = var3;
      this.block = var4.getTile(var1, var2, var3);
      this.data = var4.getData(var1, var2, var3);
   }

   public void read(DataInputStream var1) throws IOException {
      this.x = var1.readInt();
      this.y = var1.read();
      this.z = var1.readInt();
      this.block = var1.read();
      this.data = var1.read();
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeInt(this.x);
      var1.write(this.y);
      var1.writeInt(this.z);
      var1.write(this.block);
      var1.write(this.data);
   }

   public void handle(PacketListener var1) {
      var1.handleTileUpdate(this);
   }

   public int getEstimatedSize() {
      return 11;
   }
}
