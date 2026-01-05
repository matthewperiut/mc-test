package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.world.item.ItemInstance;

public class ContainerSetSlotPacket extends Packet {
   public static final int CONTAINER = 0;
   public static final int WORKBENCH = 1;
   public static final int FURNACE = 2;
   public int containerId;
   public int slot;
   public ItemInstance item;

   public ContainerSetSlotPacket() {
   }

   public ContainerSetSlotPacket(int var1, int var2, ItemInstance var3) {
      this.containerId = var1;
      this.slot = var2;
      this.item = var3 == null ? var3 : var3.copy();
   }

   public void handle(PacketListener var1) {
      var1.handleContainerSetSlot(this);
   }

   public void read(DataInputStream var1) throws IOException {
      this.containerId = var1.readByte();
      this.slot = var1.readShort();
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
      var1.writeShort(this.slot);
      if (this.item == null) {
         var1.writeShort(-1);
      } else {
         var1.writeShort(this.item.id);
         var1.writeByte(this.item.count);
         var1.writeShort(this.item.getAuxValue());
      }

   }

   public int getEstimatedSize() {
      return 8;
   }
}
