package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RemoveEntityPacket extends Packet {
   public int id;

   public RemoveEntityPacket() {
   }

   public RemoveEntityPacket(int var1) {
      this.id = var1;
   }

   public void read(DataInputStream var1) throws IOException {
      this.id = var1.readInt();
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeInt(this.id);
   }

   public void handle(PacketListener var1) {
      var1.handleRemoveEntity(this);
   }

   public int getEstimatedSize() {
      return 4;
   }
}
