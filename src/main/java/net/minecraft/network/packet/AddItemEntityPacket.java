package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.world.entity.item.ItemEntity;
import util.Mth;

public class AddItemEntityPacket extends Packet {
   public int id;
   public int x;
   public int y;
   public int z;
   public byte xa;
   public byte ya;
   public byte za;
   public int itemId;
   public int itemCount;
   public int auxValue;

   public AddItemEntityPacket() {
   }

   public AddItemEntityPacket(ItemEntity var1) {
      this.id = var1.entityId;
      this.itemId = var1.item.id;
      this.itemCount = var1.item.count;
      this.auxValue = var1.item.getAuxValue();
      this.x = Mth.floor(var1.x * (double)32.0F);
      this.y = Mth.floor(var1.y * (double)32.0F);
      this.z = Mth.floor(var1.z * (double)32.0F);
      this.xa = (byte)((int)(var1.xd * (double)128.0F));
      this.ya = (byte)((int)(var1.yd * (double)128.0F));
      this.za = (byte)((int)(var1.zd * (double)128.0F));
   }

   public void read(DataInputStream var1) throws IOException {
      this.id = var1.readInt();
      this.itemId = var1.readShort();
      this.itemCount = var1.readByte();
      this.auxValue = var1.readShort();
      this.x = var1.readInt();
      this.y = var1.readInt();
      this.z = var1.readInt();
      this.xa = var1.readByte();
      this.ya = var1.readByte();
      this.za = var1.readByte();
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeInt(this.id);
      var1.writeShort(this.itemId);
      var1.writeByte(this.itemCount);
      var1.writeShort(this.auxValue);
      var1.writeInt(this.x);
      var1.writeInt(this.y);
      var1.writeInt(this.z);
      var1.writeByte(this.xa);
      var1.writeByte(this.ya);
      var1.writeByte(this.za);
   }

   public void handle(PacketListener var1) {
      var1.handleAddItemEntity(this);
   }

   public int getEstimatedSize() {
      return 24;
   }
}
