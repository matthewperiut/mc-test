package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ChunkVisibilityPacket extends Packet {
   public int x;
   public int z;
   public boolean visible;

   public ChunkVisibilityPacket() {
      this.shouldDelay = false;
   }

   public ChunkVisibilityPacket(int var1, int var2, boolean var3) {
      this.shouldDelay = false;
      this.x = var1;
      this.z = var2;
      this.visible = var3;
   }

   public void read(DataInputStream var1) throws IOException {
      this.x = var1.readInt();
      this.z = var1.readInt();
      this.visible = var1.read() != 0;
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeInt(this.x);
      var1.writeInt(this.z);
      var1.write(this.visible ? 1 : 0);
   }

   public void handle(PacketListener var1) {
      var1.handleChunkVisibility(this);
   }

   public int getEstimatedSize() {
      return 9;
   }
}
