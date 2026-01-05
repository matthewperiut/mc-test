package net.minecraft.world.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;

public class SeedItem extends Item {
   private int resultId;

   public SeedItem(int var1, int var2) {
      super(var1);
      this.resultId = var2;
   }

   public boolean useOn(ItemInstance var1, Player var2, Level var3, int var4, int var5, int var6, int var7) {
      if (var7 != 1) {
         return false;
      } else {
         int var8 = var3.getTile(var4, var5, var6);
         if (var8 == Tile.farmland.id && var3.isEmptyTile(var4, var5 + 1, var6)) {
            var3.setTile(var4, var5 + 1, var6, this.resultId);
            --var1.count;
            return true;
         } else {
            return false;
         }
      }
   }
}
