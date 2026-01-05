package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RespawnPacket extends Packet {
   public RespawnPacket() {
   }

   public void handle(PacketListener var1) {
      var1.handleRespawn(this);
   }

   public void read(DataInputStream var1) throws IOException {
   }

   public void write(DataOutputStream var1) throws IOException {
   }

   public int getEstimatedSize() {
      return 0;
   }
}
