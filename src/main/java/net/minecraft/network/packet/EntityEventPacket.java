package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class EntityEventPacket extends Packet {
   public int entityId;
   public byte eventId;

   public EntityEventPacket() {
   }

   public EntityEventPacket(int var1, byte var2) {
      this.entityId = var1;
      this.eventId = var2;
   }

   public void read(DataInputStream var1) throws IOException {
      this.entityId = var1.readInt();
      this.eventId = var1.readByte();
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeInt(this.entityId);
      var1.writeByte(this.eventId);
   }

   public void handle(PacketListener var1) {
      var1.handleEntityEvent(this);
   }

   public int getEstimatedSize() {
      return 5;
   }
}
