package net.minecraft.world.item;

import net.minecraft.world.entity.Painting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class PaintingItem extends Item {
   public PaintingItem(int var1) {
      super(var1);
      this.maxDamage = 64;
   }

   public boolean useOn(ItemInstance var1, Player var2, Level var3, int var4, int var5, int var6, int var7) {
      if (var7 == 0) {
         return false;
      } else if (var7 == 1) {
         return false;
      } else {
         byte var8 = 0;
         if (var7 == 4) {
            var8 = 1;
         }

         if (var7 == 3) {
            var8 = 2;
         }

         if (var7 == 5) {
            var8 = 3;
         }

         Painting var9 = new Painting(var3, var4, var5, var6, var8);
         if (var9.survives()) {
            if (!var3.isOnline) {
               var3.addEntity(var9);
            }

            --var1.count;
         }

         return true;
      }
   }
}
