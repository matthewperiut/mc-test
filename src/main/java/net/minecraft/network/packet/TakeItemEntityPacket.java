package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TakeItemEntityPacket extends Packet {
   public int itemId;
   public int playerId;

   public TakeItemEntityPacket() {
   }

   public TakeItemEntityPacket(int var1, int var2) {
      this.itemId = var1;
      this.playerId = var2;
   }

   public void read(DataInputStream var1) throws IOException {
      this.itemId = var1.readInt();
      this.playerId = var1.readInt();
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeInt(this.itemId);
      var1.writeInt(this.playerId);
   }

   public void handle(PacketListener var1) {
      var1.handleTakeItemEntity(this);
   }

   public int getEstimatedSize() {
      return 8;
   }
}
