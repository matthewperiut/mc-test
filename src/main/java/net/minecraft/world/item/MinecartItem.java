package net.minecraft.world.item;

import net.minecraft.world.entity.item.Minecart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;

public class MinecartItem extends Item {
   public int type;

   public MinecartItem(int var1, int var2) {
      super(var1);
      this.maxStackSize = 1;
      this.type = var2;
   }

   public boolean useOn(ItemInstance var1, Player var2, Level var3, int var4, int var5, int var6, int var7) {
      int var8 = var3.getTile(var4, var5, var6);
      if (var8 == Tile.rail.id) {
         if (!var3.isOnline) {
            var3.addEntity(new Minecart(var3, (double)((float)var4 + 0.5F), (double)((float)var5 + 0.5F), (double)((float)var6 + 0.5F), this.type));
         }

         --var1.count;
         return true;
      } else {
         return false;
      }
   }
}
