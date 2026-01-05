package net.minecraft.world.entity.monster;

import net.minecraft.world.level.Level;

public class Giant extends Monster {
   public Giant(Level var1) {
      super(var1);
      this.textureName = "/mob/zombie.png";
      this.runSpeed = 0.5F;
      this.attackDamage = 50;
      this.health *= 10;
      this.heightOffset *= 6.0F;
      this.setSize(this.bbWidth * 6.0F, this.bbHeight * 6.0F);
   }

   protected float getWalkTargetValue(int var1, int var2, int var3) {
      return this.level.getBrightness(var1, var2, var3) - 0.5F;
   }
}
