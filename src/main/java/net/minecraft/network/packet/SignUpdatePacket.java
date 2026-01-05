package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SignUpdatePacket extends Packet {
   public int x;
   public int y;
   public int z;
   public String[] lines;

   public SignUpdatePacket() {
      this.shouldDelay = true;
   }

   public SignUpdatePacket(int var1, int var2, int var3, String[] var4) {
      this.shouldDelay = true;
      this.x = var1;
      this.y = var2;
      this.z = var3;
      this.lines = var4;
   }

   public void read(DataInputStream var1) throws IOException {
      this.x = var1.readInt();
      this.y = var1.readShort();
      this.z = var1.readInt();
      this.lines = new String[4];

      for(int var2 = 0; var2 < 4; ++var2) {
         this.lines[var2] = var1.readUTF();
      }

   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeInt(this.x);
      var1.writeShort(this.y);
      var1.writeInt(this.z);

      for(int var2 = 0; var2 < 4; ++var2) {
         var1.writeUTF(this.lines[var2]);
      }

   }

   public void handle(PacketListener var1) {
      var1.handleSignUpdate(this);
   }

   public int getEstimatedSize() {
      int var1 = 0;

      for(int var2 = 0; var2 < 4; ++var2) {
         var1 += this.lines[var2].length();
      }

      return var1;
   }
}
