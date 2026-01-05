package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.world.item.ItemInstance;

public class SetEquippedItemPacket extends Packet {
   public int entity;
   public int slot;
   public int item;
   public int auxValue;

   public SetEquippedItemPacket() {
   }

   public SetEquippedItemPacket(int var1, int var2, ItemInstance var3) {
      this.entity = var1;
      this.slot = var2;
      if (var3 == null) {
         this.item = -1;
         this.auxValue = 0;
      } else {
         this.item = var3.id;
         this.auxValue = var3.getAuxValue();
      }

   }

   public void read(DataInputStream var1) throws IOException {
      this.entity = var1.readInt();
      this.slot = var1.readShort();
      this.item = var1.readShort();
      this.auxValue = var1.readShort();
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeInt(this.entity);
      var1.writeShort(this.slot);
      var1.writeShort(this.item);
      var1.writeShort(this.auxValue);
   }

   public void handle(PacketListener var1) {
      var1.handleSetEquippedItem(this);
   }

   public int getEstimatedSize() {
      return 8;
   }
}
