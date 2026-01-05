package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SetSpawnPositionPacket extends Packet {
   public int x;
   public int y;
   public int z;

   public SetSpawnPositionPacket() {
   }

   public SetSpawnPositionPacket(int var1, int var2, int var3) {
      this.x = var1;
      this.y = var2;
      this.z = var3;
   }

   public void read(DataInputStream var1) throws IOException {
      this.x = var1.readInt();
      this.y = var1.readInt();
      this.z = var1.readInt();
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeInt(this.x);
      var1.writeInt(this.y);
      var1.writeInt(this.z);
   }

   public void handle(PacketListener var1) {
      var1.handleSetSpawn(this);
   }

   public int getEstimatedSize() {
      return 12;
   }
}
