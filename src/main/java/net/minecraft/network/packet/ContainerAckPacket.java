package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ContainerAckPacket extends Packet {
   public int containerId;
   public short uid;
   public boolean accepted;

   public ContainerAckPacket() {
   }

   public ContainerAckPacket(int var1, short var2, boolean var3) {
      this.containerId = var1;
      this.uid = var2;
      this.accepted = var3;
   }

   public void handle(PacketListener var1) {
      var1.handleContainerAck(this);
   }

   public void read(DataInputStream var1) throws IOException {
      this.containerId = var1.readByte();
      this.uid = var1.readShort();
      this.accepted = var1.readByte() != 0;
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeByte(this.containerId);
      var1.writeShort(this.uid);
      var1.writeByte(this.accepted ? 1 : 0);
   }

   public int getEstimatedSize() {
      return 4;
   }
}
