package net.minecraft.world.level.tile.entity;

import com.mojang.nbt.CompoundTag;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.SignUpdatePacket;

public class SignTileEntity extends TileEntity {
   public String[] messages = new String[]{"", "", "", ""};
   public int selectedLine = -1;

   public SignTileEntity() {
   }

   public void save(CompoundTag var1) {
      super.save(var1);
      var1.putString("Text1", this.messages[0]);
      var1.putString("Text2", this.messages[1]);
      var1.putString("Text3", this.messages[2]);
      var1.putString("Text4", this.messages[3]);
   }

   public void load(CompoundTag var1) {
      super.load(var1);

      for(int var2 = 0; var2 < 4; ++var2) {
         this.messages[var2] = var1.getString("Text" + (var2 + 1));
         if (this.messages[var2].length() > 15) {
            this.messages[var2] = this.messages[var2].substring(0, 15);
         }
      }

   }

   public Packet getUpdatePacket() {
      String[] var1 = new String[4];

      for(int var2 = 0; var2 < 4; ++var2) {
         var1[var2] = this.messages[var2];
      }

      return new SignUpdatePacket(this.x, this.y, this.z, var1);
   }
}
