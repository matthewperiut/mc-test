package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SetTimePacket extends Packet {
   public long time;

   public SetTimePacket() {
   }

   public SetTimePacket(long var1) {
      this.time = var1;
   }

   public void read(DataInputStream var1) throws IOException {
      this.time = var1.readLong();
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeLong(this.time);
   }

   public void handle(PacketListener var1) {
      var1.handleSetTime(this);
   }

   public int getEstimatedSize() {
      return 8;
   }
}
