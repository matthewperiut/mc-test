package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ContainerClosePacket extends Packet {
   public int containerId;

   public ContainerClosePacket() {
   }

   public ContainerClosePacket(int var1) {
      this.containerId = var1;
   }

   public void handle(PacketListener var1) {
      var1.handleContainerClose(this);
   }

   public void read(DataInputStream var1) throws IOException {
      this.containerId = var1.readByte();
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeByte(this.containerId);
   }

   public int getEstimatedSize() {
      return 1;
   }
}
