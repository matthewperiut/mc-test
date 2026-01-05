package net.minecraft.world.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;

public class RedStoneItem extends Item {
   public RedStoneItem(int var1) {
      super(var1);
   }

   public boolean useOn(ItemInstance var1, Player var2, Level var3, int var4, int var5, int var6, int var7) {
      if (var7 == 0) {
         --var5;
      }

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

      if (!var3.isEmptyTile(var4, var5, var6)) {
         return false;
      } else {
         if (Tile.redStoneDust.mayPlace(var3, var4, var5, var6)) {
            --var1.count;
            var3.setTile(var4, var5, var6, Tile.redStoneDust.id);
         }

         return true;
      }
   }
}
