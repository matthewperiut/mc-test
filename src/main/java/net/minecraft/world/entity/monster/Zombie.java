package net.minecraft.world.entity.monster;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import util.Mth;

public class Zombie extends Monster {
   public Zombie(Level var1) {
      super(var1);
      this.textureName = "/mob/zombie.png";
      this.runSpeed = 0.5F;
      this.attackDamage = 5;
   }

   public void aiStep() {
      if (this.level.isDay()) {
         float var1 = this.getBrightness(1.0F);
         if (var1 > 0.5F && this.level.canSeeSky(Mth.floor(this.x), Mth.floor(this.y), Mth.floor(this.z)) && this.random.nextFloat() * 30.0F < (var1 - 0.4F) * 2.0F) {
            this.onFire = 300;
         }
      }

      super.aiStep();
   }

   protected String getAmbientSound() {
      return "mob.zombie";
   }

   protected String getHurtSound() {
      return "mob.zombiehurt";
   }

   protected String getDeathSound() {
      return "mob.zombiedeath";
   }

   protected int getDeathLoot() {
      return Item.feather.id;
   }
}
