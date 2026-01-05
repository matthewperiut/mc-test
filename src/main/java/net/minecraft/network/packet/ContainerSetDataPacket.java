package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ContainerSetDataPacket extends Packet {
   public int containerId;
   public int id;
   public int value;

   public ContainerSetDataPacket() {
   }

   public ContainerSetDataPacket(int var1, int var2, int var3) {
      this.containerId = var1;
      this.id = var2;
      this.value = var3;
   }

   public void handle(PacketListener var1) {
      var1.handleContainerSetData(this);
   }

   public void read(DataInputStream var1) throws IOException {
      this.containerId = var1.readByte();
      this.id = var1.readShort();
      this.value = var1.readShort();
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeByte(this.containerId);
      var1.writeShort(this.id);
      var1.writeShort(this.value);
   }

   public int getEstimatedSize() {
      return 5;
   }
}
