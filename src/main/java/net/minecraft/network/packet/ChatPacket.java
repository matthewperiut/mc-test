package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ChatPacket extends Packet {
   public String message;

   public ChatPacket() {
   }

   public ChatPacket(String var1) {
      this.message = var1;
   }

   public void read(DataInputStream var1) throws IOException {
      this.message = var1.readUTF();
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeUTF(this.message);
   }

   public void handle(PacketListener var1) {
      var1.handleChat(this);
   }

   public int getEstimatedSize() {
      return this.message.length();
   }
}
