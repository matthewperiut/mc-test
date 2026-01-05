package net.minecraft.world.level.tile.entity;

import com.mojang.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;

public class MusicTileEntity extends TileEntity {
   public byte note = 0;
   public boolean on = false;

   public MusicTileEntity() {
   }

   public void save(CompoundTag var1) {
      super.save(var1);
      var1.putByte("note", this.note);
   }

   public void load(CompoundTag var1) {
      super.load(var1);
      this.note = var1.getByte("note");
      if (this.note < 0) {
         this.note = 0;
      }

      if (this.note > 24) {
         this.note = 24;
      }

   }

   public void tune() {
      this.note = (byte)((this.note + 1) % 25);
      this.setChanged();
   }

   public void playNote(Level var1, int var2, int var3, int var4) {
      if (var1.getMaterial(var2, var3 + 1, var4) == Material.air) {
         Material var5 = var1.getMaterial(var2, var3 - 1, var4);
         byte var6 = 0;
         if (var5 == Material.stone) {
            var6 = 1;
         }

         if (var5 == Material.sand) {
            var6 = 2;
         }

         if (var5 == Material.glass) {
            var6 = 3;
         }

         if (var5 == Material.wood) {
            var6 = 4;
         }

         var1.tileEvent(var2, var3, var4, var6, this.note);
      }
   }
}
