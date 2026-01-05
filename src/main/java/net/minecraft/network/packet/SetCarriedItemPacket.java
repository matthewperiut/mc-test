package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SetCarriedItemPacket extends Packet {
   public int slot;

   public SetCarriedItemPacket() {
   }

   public SetCarriedItemPacket(int var1) {
      this.slot = var1;
   }

   public void read(DataInputStream var1) throws IOException {
      this.slot = var1.readShort();
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeShort(this.slot);
   }

   public void handle(PacketListener var1) {
      var1.handleSetCarriedItem(this);
   }

   public int getEstimatedSize() {
      return 2;
   }
}
