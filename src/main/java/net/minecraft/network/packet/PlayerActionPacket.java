package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PlayerActionPacket extends Packet {
   public static final int START_DESTROY_BLOCK = 0;
   public static final int CONTINUE_DESTROY_BLOCK = 1;
   public static final int STOP_DESTROY_BLOCK = 2;
   public static final int GET_UPDATED_BLOCK = 3;
   public static final int DROP_ITEM = 4;
   public int x;
   public int y;
   public int z;
   public int face;
   public int action;

   public PlayerActionPacket() {
   }

   public PlayerActionPacket(int var1, int var2, int var3, int var4, int var5) {
      this.action = var1;
      this.x = var2;
      this.y = var3;
      this.z = var4;
      this.face = var5;
   }

   public void read(DataInputStream var1) throws IOException {
      this.action = var1.read();
      this.x = var1.readInt();
      this.y = var1.read();
      this.z = var1.readInt();
      this.face = var1.read();
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.write(this.action);
      var1.writeInt(this.x);
      var1.write(this.y);
      var1.writeInt(this.z);
      var1.write(this.face);
   }

   public void handle(PacketListener var1) {
      var1.handlePlayerAction(this);
   }

   public int getEstimatedSize() {
      return 11;
   }
}
