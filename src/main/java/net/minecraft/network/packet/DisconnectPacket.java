package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DisconnectPacket extends Packet {
   public String reason;

   public DisconnectPacket() {
   }

   public DisconnectPacket(String var1) {
      this.reason = var1;
   }

   public void read(DataInputStream var1) throws IOException {
      this.reason = var1.readUTF();
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeUTF(this.reason);
   }

   public void handle(PacketListener var1) {
      var1.handleDisconnect(this);
   }

   public int getEstimatedSize() {
      return this.reason.length();
   }
}
