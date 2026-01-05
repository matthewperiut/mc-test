package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import net.minecraft.world.item.ItemInstance;

public class ContainerSetContentPacket extends Packet {
   public int containerId;
   public ItemInstance[] items;

   public ContainerSetContentPacket() {
   }

   public ContainerSetContentPacket(int var1, List<ItemInstance> var2) {
      this.containerId = var1;
      this.items = new ItemInstance[var2.size()];

      for(int var3 = 0; var3 < this.items.length; ++var3) {
         ItemInstance var4 = (ItemInstance)var2.get(var3);
         this.items[var3] = var4 == null ? null : var4.copy();
      }

   }

   public void read(DataInputStream var1) throws IOException {
      this.containerId = var1.readByte();
      short var2 = var1.readShort();
      this.items = new ItemInstance[var2];

      for(int var3 = 0; var3 < var2; ++var3) {
         short var4 = var1.readShort();
         if (var4 >= 0) {
            byte var5 = var1.readByte();
            short var6 = var1.readShort();
            this.items[var3] = new ItemInstance(var4, var5, var6);
         }
      }

   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeByte(this.containerId);
      var1.writeShort(this.items.length);

      for(int var2 = 0; var2 < this.items.length; ++var2) {
         if (this.items[var2] == null) {
            var1.writeShort(-1);
         } else {
            var1.writeShort((short)this.items[var2].id);
            var1.writeByte((byte)this.items[var2].count);
            var1.writeShort((short)this.items[var2].getAuxValue());
         }
      }

   }

   public void handle(PacketListener var1) {
      var1.handleContainerContent(this);
   }

   public int getEstimatedSize() {
      return 3 + this.items.length * 5;
   }
}
