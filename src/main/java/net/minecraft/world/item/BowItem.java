package net.minecraft.world.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;

public class BowItem extends Item {
   public BowItem(int var1) {
      super(var1);
      this.maxStackSize = 1;
   }

   public ItemInstance use(ItemInstance var1, Level var2, Player var3) {
      if (var3.inventory.removeResource(Item.arrow.id)) {
         var2.playSound(var3, "random.bow", 1.0F, 1.0F / (random.nextFloat() * 0.4F + 0.8F));
         if (!var2.isOnline) {
            var2.addEntity(new Arrow(var2, var3));
         }
      }

      return var1;
   }
}
