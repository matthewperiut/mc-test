package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PreLoginPacket extends Packet {
   public String userName;

   public PreLoginPacket() {
   }

   public PreLoginPacket(String var1) {
      this.userName = var1;
   }

   public void read(DataInputStream var1) throws IOException {
      this.userName = var1.readUTF();
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeUTF(this.userName);
   }

   public void handle(PacketListener var1) {
      var1.handlePreLogin(this);
   }

   public int getEstimatedSize() {
      return 4 + this.userName.length() + 4;
   }
}
