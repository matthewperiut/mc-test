package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import net.minecraft.world.entity.SynchedEntityData;

public class SetEntityDataPacket extends Packet {
   public int id;
   private List<SynchedEntityData.DataItem> packedItems;

   public SetEntityDataPacket() {
   }

   public SetEntityDataPacket(int var1, SynchedEntityData var2) {
      this.id = var1;
      this.packedItems = var2.packDirty();
   }

   public void read(DataInputStream var1) throws IOException {
      this.id = var1.readInt();
      this.packedItems = SynchedEntityData.unpack(var1);
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeInt(this.id);
      SynchedEntityData.pack(this.packedItems, var1);
   }

   public void handle(PacketListener var1) {
      var1.handleSetEntityData(this);
   }

   public int getEstimatedSize() {
      return 5;
   }

   public List<SynchedEntityData.DataItem> getUnpackedData() {
      return this.packedItems;
   }
}
