package net.minecraft.world.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class FoodItem extends Item {
   private int nutrition;

   public FoodItem(int var1, int var2) {
      super(var1);
      this.nutrition = var2;
      this.maxStackSize = 1;
   }

   public ItemInstance use(ItemInstance var1, Level var2, Player var3) {
      --var1.count;
      var3.heal(this.nutrition);
      return var1;
   }
}
