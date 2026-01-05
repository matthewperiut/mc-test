package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.world.entity.Entity;

public class AnimatePacket extends Packet {
   public static final int SWING = 1;
   public static final int HURT = 2;
   public int id;
   public int action;

   public AnimatePacket() {
   }

   public AnimatePacket(Entity var1, int var2) {
      this.id = var1.entityId;
      this.action = var2;
   }

   public void read(DataInputStream var1) throws IOException {
      this.id = var1.readInt();
      this.action = var1.readByte();
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeInt(this.id);
      var1.writeByte(this.action);
   }

   public void handle(PacketListener var1) {
      var1.handleAnimate(this);
   }

   public int getEstimatedSize() {
      return 5;
   }
}
