package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.world.entity.Painting;

public class AddPaintingPacket extends Packet {
   public int id;
   public int x;
   public int y;
   public int z;
   public int dir;
   public String motive;

   public AddPaintingPacket() {
   }

   public AddPaintingPacket(Painting var1) {
      this.id = var1.entityId;
      this.x = var1.xTile;
      this.y = var1.yTile;
      this.z = var1.zTile;
      this.dir = var1.dir;
      this.motive = var1.motive.name;
   }

   public void read(DataInputStream var1) throws IOException {
      this.id = var1.readInt();
      this.motive = var1.readUTF();
      this.x = var1.readInt();
      this.y = var1.readInt();
      this.z = var1.readInt();
      this.dir = var1.readInt();
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeInt(this.id);
      var1.writeUTF(this.motive);
      var1.writeInt(this.x);
      var1.writeInt(this.y);
      var1.writeInt(this.z);
      var1.writeInt(this.dir);
   }

   public void handle(PacketListener var1) {
      var1.handleAddPainting(this);
   }

   public int getEstimatedSize() {
      return 24;
   }
}
