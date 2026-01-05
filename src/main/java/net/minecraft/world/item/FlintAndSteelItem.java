package net.minecraft.world.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;

public class FlintAndSteelItem extends Item {
   public FlintAndSteelItem(int var1) {
      super(var1);
      this.maxStackSize = 1;
      this.maxDamage = 64;
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

      int var8 = var3.getTile(var4, var5, var6);
      if (var8 == 0) {
         var3.playSound((double)var4 + (double)0.5F, (double)var5 + (double)0.5F, (double)var6 + (double)0.5F, "fire.ignite", 1.0F, random.nextFloat() * 0.4F + 0.8F);
         var3.setTile(var4, var5, var6, Tile.fire.id);
      }

      var1.hurt(1);
      return true;
   }
}
