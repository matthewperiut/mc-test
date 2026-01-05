package net.minecraft.world.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class BowlFoodItem extends FoodItem {
   public BowlFoodItem(int var1, int var2) {
      super(var1, var2);
   }

   public ItemInstance use(ItemInstance var1, Level var2, Player var3) {
      super.use(var1, var2, var3);
      return new ItemInstance(Item.bowl);
   }
}
