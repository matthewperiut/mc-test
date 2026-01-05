package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemInstance;
import util.Mth;

public class AddPlayerPacket extends Packet {
   public int id;
   public String name;
   public int x;
   public int y;
   public int z;
   public byte yRot;
   public byte xRot;
   public int carriedItem;

   public AddPlayerPacket() {
   }

   public AddPlayerPacket(Player var1) {
      this.id = var1.entityId;
      this.name = var1.name;
      this.x = Mth.floor(var1.x * (double)32.0F);
      this.y = Mth.floor(var1.y * (double)32.0F);
      this.z = Mth.floor(var1.z * (double)32.0F);
      this.yRot = (byte)((int)(var1.yRot * 256.0F / 360.0F));
      this.xRot = (byte)((int)(var1.xRot * 256.0F / 360.0F));
      ItemInstance var2 = var1.inventory.getSelected();
      this.carriedItem = var2 == null ? 0 : var2.id;
   }

   public void read(DataInputStream var1) throws IOException {
      this.id = var1.readInt();
      this.name = var1.readUTF();
      this.x = var1.readInt();
      this.y = var1.readInt();
      this.z = var1.readInt();
      this.yRot = var1.readByte();
      this.xRot = var1.readByte();
      this.carriedItem = var1.readShort();
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeInt(this.id);
      var1.writeUTF(this.name);
      var1.writeInt(this.x);
      var1.writeInt(this.y);
      var1.writeInt(this.z);
      var1.writeByte(this.yRot);
      var1.writeByte(this.xRot);
      var1.writeShort(this.carriedItem);
   }

   public void handle(PacketListener var1) {
      var1.handleAddPlayer(this);
   }

   public int getEstimatedSize() {
      return 28;
   }
}
