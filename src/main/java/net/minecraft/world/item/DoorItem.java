package net.minecraft.world.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;
import util.Mth;

public class DoorItem extends Item {
   private Material material;

   public DoorItem(int var1, Material var2) {
      super(var1);
      this.material = var2;
      this.maxDamage = 64;
      this.maxStackSize = 1;
   }

   public boolean useOn(ItemInstance var1, Player var2, Level var3, int var4, int var5, int var6, int var7) {
      if (var7 != 1) {
         return false;
      } else {
         ++var5;
         Tile var8;
         if (this.material == Material.wood) {
            var8 = Tile.door_wood;
         } else {
            var8 = Tile.door_iron;
         }

         if (!var8.mayPlace(var3, var4, var5, var6)) {
            return false;
         } else {
            int var9 = Mth.floor((double)((var2.yRot + 180.0F) * 4.0F / 360.0F) - (double)0.5F) & 3;
            byte var10 = 0;
            byte var11 = 0;
            if (var9 == 0) {
               var11 = 1;
            }

            if (var9 == 1) {
               var10 = -1;
            }

            if (var9 == 2) {
               var11 = -1;
            }

            if (var9 == 3) {
               var10 = 1;
            }

            int var12 = (var3.isSolidTile(var4 - var10, var5, var6 - var11) ? 1 : 0) + (var3.isSolidTile(var4 - var10, var5 + 1, var6 - var11) ? 1 : 0);
            int var13 = (var3.isSolidTile(var4 + var10, var5, var6 + var11) ? 1 : 0) + (var3.isSolidTile(var4 + var10, var5 + 1, var6 + var11) ? 1 : 0);
            boolean var14 = var3.getTile(var4 - var10, var5, var6 - var11) == var8.id || var3.getTile(var4 - var10, var5 + 1, var6 - var11) == var8.id;
            boolean var15 = var3.getTile(var4 + var10, var5, var6 + var11) == var8.id || var3.getTile(var4 + var10, var5 + 1, var6 + var11) == var8.id;
            boolean var16 = false;
            if (var14 && !var15) {
               var16 = true;
            } else if (var13 > var12) {
               var16 = true;
            }

            if (var16) {
               var9 = var9 - 1 & 3;
               var9 += 4;
            }

            var3.setTile(var4, var5, var6, var8.id);
            var3.setData(var4, var5, var6, var9);
            var3.setTile(var4, var5 + 1, var6, var8.id);
            var3.setData(var4, var5 + 1, var6, var9 + 8);
            --var1.count;
            return true;
         }
      }
   }
}
