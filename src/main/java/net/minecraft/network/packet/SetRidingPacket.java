package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.world.entity.Entity;

public class SetRidingPacket extends Packet {
   public int riderId;
   public int riddenId;

   public SetRidingPacket() {
   }

   public SetRidingPacket(Entity var1, Entity var2) {
      this.riderId = var1.entityId;
      this.riddenId = var2 != null ? var2.entityId : -1;
   }

   public int getEstimatedSize() {
      return 8;
   }

   public void read(DataInputStream var1) throws IOException {
      this.riderId = var1.readInt();
      this.riddenId = var1.readInt();
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeInt(this.riderId);
      var1.writeInt(this.riddenId);
   }

   public void handle(PacketListener var1) {
      var1.handleRidePacket(this);
   }
}
