package net.minecraft.world.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.entity.SignTileEntity;
import util.Mth;

public class SignItem extends Item {
   public SignItem(int var1) {
      super(var1);
      this.maxDamage = 64;
      this.maxStackSize = 1;
   }

   public boolean useOn(ItemInstance var1, Player var2, Level var3, int var4, int var5, int var6, int var7) {
      if (var7 == 0) {
         return false;
      } else if (!var3.getMaterial(var4, var5, var6).isSolid()) {
         return false;
      } else {
         if (var7 == 1) {
            ++var5;
         }

         if (var7 == 2) {
            --var6;
         }

         if (var7 == 3) {
            ++var6;
         }

         if (var7 == 4) {
            --var4;
         }

         if (var7 == 5) {
            ++var4;
         }

         if (!Tile.sign.mayPlace(var3, var4, var5, var6)) {
            return false;
         } else {
            if (var7 == 1) {
               var3.setTileAndData(var4, var5, var6, Tile.sign.id, Mth.floor((double)((var2.yRot + 180.0F) * 16.0F / 360.0F) + (double)0.5F) & 15);
            } else {
               var3.setTileAndData(var4, var5, var6, Tile.wallSign.id, var7);
            }

            --var1.count;
            SignTileEntity var8 = (SignTileEntity)var3.getTileEntity(var4, var5, var6);
            if (var8 != null) {
               var2.openTextEdit(var8);
            }

            return true;
         }
      }
   }
}
