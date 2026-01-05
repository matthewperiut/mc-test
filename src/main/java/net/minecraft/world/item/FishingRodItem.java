package net.minecraft.world.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.Level;

public class FishingRodItem extends Item {
   public FishingRodItem(int var1) {
      super(var1);
      this.maxDamage = 64;
   }

   public boolean isHandEquipped() {
      return true;
   }

   public boolean isMirroredArt() {
      return true;
   }

   public ItemInstance use(ItemInstance var1, Level var2, Player var3) {
      if (var3.fishing != null) {
         int var4 = var3.fishing.retrieve();
         var1.hurt(var4);
         var3.swing();
      } else {
         var2.playSound(var3, "random.bow", 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
         if (!var2.isOnline) {
            var2.addEntity(new FishingHook(var2, var3));
         }

         var3.swing();
      }

      return var1;
   }
}
