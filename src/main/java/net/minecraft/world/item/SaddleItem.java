package net.minecraft.world.item;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Pig;

public class SaddleItem extends Item {
   public SaddleItem(int var1) {
      super(var1);
      this.maxStackSize = 1;
      this.maxDamage = 64;
   }

   public void interactEnemy(ItemInstance var1, Mob var2) {
      if (var2 instanceof Pig) {
         Pig var3 = (Pig)var2;
         if (!var3.hasSaddle()) {
            var3.setSaddle(true);
            --var1.count;
         }
      }

   }

   public void hurtEnemy(ItemInstance var1, Mob var2) {
      this.interactEnemy(var1, var2);
   }
}
