package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class InteractPacket extends Packet {
   public static final int INTERACT = 0;
   public static final int ATTACK = 1;
   public int source;
   public int target;
   public int action;

   public InteractPacket() {
   }

   public InteractPacket(int var1, int var2, int var3) {
      this.source = var1;
      this.target = var2;
      this.action = var3;
   }

   public void read(DataInputStream var1) throws IOException {
      this.source = var1.readInt();
      this.target = var1.readInt();
      this.action = var1.readByte();
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeInt(this.source);
      var1.writeInt(this.target);
      var1.writeByte(this.action);
   }

   public void handle(PacketListener var1) {
      var1.handleInteract(this);
   }

   public int getEstimatedSize() {
      return 9;
   }
}
