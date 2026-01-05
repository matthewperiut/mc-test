package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.world.item.ItemInstance;

public class UseItemPacket extends Packet {
   public int x;
   public int y;
   public int z;
   public int face;
   public ItemInstance item;

   public UseItemPacket() {
   }

   public UseItemPacket(int var1, int var2, int var3, int var4, ItemInstance var5) {
      this.x = var1;
      this.y = var2;
      this.z = var3;
      this.face = var4;
      this.item = var5;
   }

   public void read(DataInputStream var1) throws IOException {
      this.x = var1.readInt();
      this.y = var1.read();
      this.z = var1.readInt();
      this.face = var1.read();
      short var2 = var1.readShort();
      if (var2 >= 0) {
         byte var3 = var1.readByte();
         short var4 = var1.readShort();
         this.item = new ItemInstance(var2, var3, var4);
      } else {
         this.item = null;
      }

   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeInt(this.x);
      var1.write(this.y);
      var1.writeInt(this.z);
      var1.write(this.face);
      if (this.item == null) {
         var1.writeShort(-1);
      } else {
         var1.writeShort(this.item.id);
         var1.writeByte(this.item.count);
         var1.writeShort(this.item.getAuxValue());
      }

   }

   public void handle(PacketListener var1) {
      var1.handleUseItem(this);
   }

   public int getEstimatedSize() {
      return 15;
   }
}
