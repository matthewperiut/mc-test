package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.world.item.ItemInstance;

public class ContainerClickPacket extends Packet {
   public int containerId;
   public int slotNum;
   public int buttonNum;
   public short uid;
   public ItemInstance item;

   public ContainerClickPacket() {
   }

   public ContainerClickPacket(int var1, int var2, int var3, ItemInstance var4, short var5) {
      this.containerId = var1;
      this.slotNum = var2;
      this.buttonNum = var3;
      this.item = var4;
      this.uid = var5;
   }

   public void handle(PacketListener var1) {
      var1.handleContainerClick(this);
   }

   public void read(DataInputStream var1) throws IOException {
      this.containerId = var1.readByte();
      this.slotNum = var1.readShort();
      this.buttonNum = var1.readByte();
      this.uid = var1.readShort();
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
      var1.writeByte(this.containerId);
      var1.writeShort(this.slotNum);
      var1.writeByte(this.buttonNum);
      var1.writeShort(this.uid);
      if (this.item == null) {
         var1.writeShort(-1);
      } else {
         var1.writeShort(this.item.id);
         var1.writeByte(this.item.count);
         var1.writeShort(this.item.getAuxValue());
      }

   }

   public int getEstimatedSize() {
      return 11;
   }
}
