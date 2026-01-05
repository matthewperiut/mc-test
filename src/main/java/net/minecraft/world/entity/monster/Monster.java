package net.minecraft.world.entity.monster;

import com.mojang.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import util.Mth;

public class Monster extends PathfinderMob implements Enemy {
   protected int attackDamage = 2;

   public Monster(Level var1) {
      super(var1);
      this.health = 20;
   }

   public void aiStep() {
      float var1 = this.getBrightness(1.0F);
      if (var1 > 0.5F) {
         this.noActionTime += 2;
      }

      super.aiStep();
   }

   public void tick() {
      super.tick();
      if (this.level.difficulty == 0) {
         this.remove();
      }

   }

   protected Entity findAttackTarget() {
      Player var1 = this.level.getNearestPlayer(this, (double)16.0F);
      return var1 != null && this.canSee(var1) ? var1 : null;
   }

   public boolean hurt(Entity var1, int var2) {
      if (super.hurt(var1, var2)) {
         if (this.rider != var1 && this.riding != var1) {
            if (var1 != this) {
               this.attackTarget = var1;
            }

            return true;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   protected void checkHurtTarget(Entity var1, float var2) {
      if ((double)var2 < (double)2.5F && var1.bb.y1 > this.bb.y0 && var1.bb.y0 < this.bb.y1) {
         this.attackTime = 20;
         var1.hurt(this, this.attackDamage);
      }

   }

   protected float getWalkTargetValue(int var1, int var2, int var3) {
      return 0.5F - this.level.getBrightness(var1, var2, var3);
   }

   public void addAdditonalSaveData(CompoundTag var1) {
      super.addAdditonalSaveData(var1);
   }

   public void readAdditionalSaveData(CompoundTag var1) {
      super.readAdditionalSaveData(var1);
   }

   public boolean canSpawn() {
      int var1 = Mth.floor(this.x);
      int var2 = Mth.floor(this.bb.y0);
      int var3 = Mth.floor(this.z);
      if (this.level.getBrightness(LightLayer.Sky, var1, var2, var3) > this.random.nextInt(32)) {
         return false;
      } else {
         int var4 = this.level.getRawBrightness(var1, var2, var3);
         return var4 <= this.random.nextInt(8) && super.canSpawn();
      }
   }
}
