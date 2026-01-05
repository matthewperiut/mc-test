package net.minecraft.world.entity.monster;

import com.mojang.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import util.Mth;

public class Spider extends Monster {
   public Spider(Level var1) {
      super(var1);
      this.textureName = "/mob/spider.png";
      this.setSize(1.4F, 0.9F);
      this.runSpeed = 0.8F;
   }

   public double getRideHeight() {
      return (double)this.bbHeight * (double)0.75F - (double)0.5F;
   }

   protected Entity findAttackTarget() {
      float var1 = this.getBrightness(1.0F);
      if (var1 < 0.5F) {
         double var2 = (double)16.0F;
         return this.level.getNearestPlayer(this, var2);
      } else {
         return null;
      }
   }

   protected String getAmbientSound() {
      return "mob.spider";
   }

   protected String getHurtSound() {
      return "mob.spider";
   }

   protected String getDeathSound() {
      return "mob.spiderdeath";
   }

   protected void checkHurtTarget(Entity var1, float var2) {
      float var3 = this.getBrightness(1.0F);
      if (var3 > 0.5F && this.random.nextInt(100) == 0) {
         this.attackTarget = null;
      } else {
         if (var2 > 2.0F && var2 < 6.0F && this.random.nextInt(10) == 0) {
            if (this.onGround) {
               double var4 = var1.x - this.x;
               double var6 = var1.z - this.z;
               float var8 = Mth.sqrt(var4 * var4 + var6 * var6);
               this.xd = var4 / (double)var8 * (double)0.5F * (double)0.8F + this.xd * (double)0.2F;
               this.zd = var6 / (double)var8 * (double)0.5F * (double)0.8F + this.zd * (double)0.2F;
               this.yd = (double)0.4F;
            }
         } else {
            super.checkHurtTarget(var1, var2);
         }

      }
   }

   public void addAdditonalSaveData(CompoundTag var1) {
      super.addAdditonalSaveData(var1);
   }

   public void readAdditionalSaveData(CompoundTag var1) {
      super.readAdditionalSaveData(var1);
   }

   protected int getDeathLoot() {
      return Item.string.id;
   }

   public boolean onLadder() {
      return this.horizontalCollision;
   }
}
