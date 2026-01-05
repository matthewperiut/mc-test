package net.minecraft.world.entity.monster;

import com.mojang.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.Level;
import util.Mth;

public class Skeleton extends Monster {
   private static final ItemInstance bow;

   public Skeleton(Level var1) {
      super(var1);
      this.textureName = "/mob/skeleton.png";
   }

   protected String getAmbientSound() {
      return "mob.skeleton";
   }

   protected String getHurtSound() {
      return "mob.skeletonhurt";
   }

   protected String getDeathSound() {
      return "mob.skeletonhurt";
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

   protected void checkHurtTarget(Entity var1, float var2) {
      if (var2 < 10.0F) {
         double var3 = var1.x - this.x;
         double var5 = var1.z - this.z;
         if (this.attackTime == 0) {
            Arrow var7 = new Arrow(this.level, this);
            ++var7.y;
            double var8 = var1.y - (double)0.2F - var7.y;
            float var10 = Mth.sqrt(var3 * var3 + var5 * var5) * 0.2F;
            this.level.playSound(this, "random.bow", 1.0F, 1.0F / (this.random.nextFloat() * 0.4F + 0.8F));
            this.level.addEntity(var7);
            var7.shoot(var3, var8 + (double)var10, var5, 0.6F, 12.0F);
            this.attackTime = 30;
         }

         this.yRot = (float)(Math.atan2(var5, var3) * (double)180.0F / (double)(float)Math.PI) - 90.0F;
         this.holdGround = true;
      }

   }

   public void addAdditonalSaveData(CompoundTag var1) {
      super.addAdditonalSaveData(var1);
   }

   public void readAdditionalSaveData(CompoundTag var1) {
      super.readAdditionalSaveData(var1);
   }

   protected int getDeathLoot() {
      return Item.arrow.id;
   }

   protected void dropDeathLoot() {
      int var1 = this.random.nextInt(3);

      for(int var2 = 0; var2 < var1; ++var2) {
         this.spawnAtLocation(Item.arrow.id, 1);
      }

      var1 = this.random.nextInt(3);

      for(int var4 = 0; var4 < var1; ++var4) {
         this.spawnAtLocation(Item.bone.id, 1);
      }

   }

   public ItemInstance getCarriedItem() {
      return bow;
   }

   static {
      bow = new ItemInstance(Item.bow, 1);
   }
}
