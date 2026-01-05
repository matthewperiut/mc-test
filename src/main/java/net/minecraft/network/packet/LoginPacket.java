package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class LoginPacket extends Packet {
   public int clientVersion;
   public String userName;
   public String loginKey;
   public long seed;
   public byte dimension;

   public LoginPacket() {
   }

   public LoginPacket(String var1, String var2, int var3) {
      this.userName = var1;
      this.loginKey = var2;
      this.clientVersion = var3;
   }

   public LoginPacket(String var1, String var2, int var3, long var4, byte var6) {
      this.userName = var1;
      this.loginKey = var2;
      this.clientVersion = var3;
      this.seed = var4;
      this.dimension = var6;
   }

   public void read(DataInputStream var1) throws IOException {
      this.clientVersion = var1.readInt();
      this.userName = var1.readUTF();
      this.loginKey = var1.readUTF();
      this.seed = var1.readLong();
      this.dimension = var1.readByte();
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeInt(this.clientVersion);
      var1.writeUTF(this.userName);
      var1.writeUTF(this.loginKey);
      var1.writeLong(this.seed);
      var1.writeByte(this.dimension);
   }

   public void handle(PacketListener var1) {
      var1.handleLogin(this);
   }

   public int getEstimatedSize() {
      return 4 + this.userName.length() + this.loginKey.length() + 4 + 5;
   }
}
