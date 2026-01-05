package net.minecraft.world.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.level.Level;

public class SnowballItem extends Item {
   public SnowballItem(int var1) {
      super(var1);
      this.maxStackSize = 16;
   }

   public ItemInstance use(ItemInstance var1, Level var2, Player var3) {
      --var1.count;
      var2.playSound(var3, "random.bow", 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
      if (!var2.isOnline) {
         var2.addEntity(new Snowball(var2, var3));
      }

      return var1;
   }
}
