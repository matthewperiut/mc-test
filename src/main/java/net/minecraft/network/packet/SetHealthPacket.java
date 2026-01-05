package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SetHealthPacket extends Packet {
   public int health;

   public SetHealthPacket() {
   }

   public SetHealthPacket(int var1) {
      this.health = var1;
   }

   public void read(DataInputStream var1) throws IOException {
      this.health = var1.readShort();
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeShort(this.health);
   }

   public void handle(PacketListener var1) {
      var1.handleSetHealth(this);
   }

   public int getEstimatedSize() {
      return 2;
   }
}
