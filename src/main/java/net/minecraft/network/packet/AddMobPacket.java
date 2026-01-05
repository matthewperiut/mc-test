package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import net.minecraft.world.entity.EntityIO;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SynchedEntityData;
import util.Mth;

public class AddMobPacket extends Packet {
   public int id;
   public byte type;
   public int x;
   public int y;
   public int z;
   public byte yRot;
   public byte xRot;
   private SynchedEntityData entityData;
   private List<SynchedEntityData.DataItem> unpack;

   public AddMobPacket() {
   }

   public AddMobPacket(Mob var1) {
      this.id = var1.entityId;
      this.type = (byte)EntityIO.getId(var1);
      this.x = Mth.floor(var1.x * (double)32.0F);
      this.y = Mth.floor(var1.y * (double)32.0F);
      this.z = Mth.floor(var1.z * (double)32.0F);
      this.yRot = (byte)((int)(var1.yRot * 256.0F / 360.0F));
      this.xRot = (byte)((int)(var1.xRot * 256.0F / 360.0F));
      this.entityData = var1.getEntityData();
   }

   public void read(DataInputStream var1) throws IOException {
      this.id = var1.readInt();
      this.type = var1.readByte();
      this.x = var1.readInt();
      this.y = var1.readInt();
      this.z = var1.readInt();
      this.yRot = var1.readByte();
      this.xRot = var1.readByte();
      this.unpack = SynchedEntityData.unpack(var1);
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeInt(this.id);
      var1.writeByte(this.type);
      var1.writeInt(this.x);
      var1.writeInt(this.y);
      var1.writeInt(this.z);
      var1.writeByte(this.yRot);
      var1.writeByte(this.xRot);
      this.entityData.packAll(var1);
   }

   public void handle(PacketListener var1) {
      var1.handleAddMob(this);
   }

   public int getEstimatedSize() {
      return 20;
   }

   public List<SynchedEntityData.DataItem> getUnpackedData() {
      return this.unpack;
   }
}
