package net.minecraft.world.entity.animal;

import com.mojang.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class Chicken extends Animal {
   public boolean sheared = false;
   public float flap = 0.0F;
   public float flapSpeed = 0.0F;
   public float oFlapSpeed;
   public float oFlap;
   public float flapping = 1.0F;
   public int eggTime;

   public Chicken(Level var1) {
      super(var1);
      this.textureName = "/mob/chicken.png";
      this.setSize(0.3F, 0.4F);
      this.health = 4;
      this.eggTime = this.random.nextInt(6000) + 6000;
   }

   public void aiStep() {
      super.aiStep();
      this.oFlap = this.flap;
      this.oFlapSpeed = this.flapSpeed;
      this.flapSpeed = (float)((double)this.flapSpeed + (double)(this.onGround ? -1 : 4) * 0.3);
      if (this.flapSpeed < 0.0F) {
         this.flapSpeed = 0.0F;
      }

      if (this.flapSpeed > 1.0F) {
         this.flapSpeed = 1.0F;
      }

      if (!this.onGround && this.flapping < 1.0F) {
         this.flapping = 1.0F;
      }

      this.flapping = (float)((double)this.flapping * 0.9);
      if (!this.onGround && this.yd < (double)0.0F) {
         this.yd *= 0.6;
      }

      this.flap += this.flapping * 2.0F;
      if (!this.level.isOnline && --this.eggTime <= 0) {
         this.level.playSound(this, "mob.chickenplop", 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
         this.spawnAtLocation(Item.egg.id, 1);
         this.eggTime = this.random.nextInt(6000) + 6000;
      }

   }

   protected void causeFallDamage(float var1) {
   }

   public void addAdditonalSaveData(CompoundTag var1) {
      super.addAdditonalSaveData(var1);
   }

   public void readAdditionalSaveData(CompoundTag var1) {
      super.readAdditionalSaveData(var1);
   }

   protected String getAmbientSound() {
      return "mob.chicken";
   }

   protected String getHurtSound() {
      return "mob.chickenhurt";
   }

   protected String getDeathSound() {
      return "mob.chickenhurt";
   }

   protected int getDeathLoot() {
      return Item.feather.id;
   }
}
