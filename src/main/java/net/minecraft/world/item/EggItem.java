package net.minecraft.world.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.level.Level;

public class EggItem extends Item {
   public EggItem(int var1) {
      super(var1);
      this.maxStackSize = 16;
   }

   public ItemInstance use(ItemInstance var1, Level var2, Player var3) {
      --var1.count;
      var2.playSound(var3, "random.bow", 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
      if (!var2.isOnline) {
         var2.addEntity(new ThrownEgg(var2, var3));
      }

      return var1;
   }
}
