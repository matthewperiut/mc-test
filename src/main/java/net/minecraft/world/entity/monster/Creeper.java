package net.minecraft.world.entity.monster;

import com.mojang.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class Creeper extends Monster {
   private static final int DATA_SWELL_DIR = 16;
   int swell;
   int oldSwell;
   private static final int MAX_SWELL = 30;

   public Creeper(Level var1) {
      super(var1);
      this.textureName = "/mob/creeper.png";
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(16, -1);
   }

   public void addAdditonalSaveData(CompoundTag var1) {
      super.addAdditonalSaveData(var1);
   }

   public void readAdditionalSaveData(CompoundTag var1) {
      super.readAdditionalSaveData(var1);
   }

   public void tick() {
      this.oldSwell = this.swell;
      if (this.level.isOnline) {
         int var1 = this.getSwellDir();
         if (var1 > 0 && this.swell == 0) {
            this.level.playSound(this, "random.fuse", 1.0F, 0.5F);
         }

         this.swell += var1;
         if (this.swell < 0) {
            this.swell = 0;
         }

         if (this.swell >= 30) {
            this.swell = 30;
         }
      }

      super.tick();
   }

   protected String getHurtSound() {
      return "mob.creeper";
   }

   protected String getDeathSound() {
      return "mob.creeperdeath";
   }

   public void die(Entity var1) {
      super.die(var1);
      if (var1 instanceof Skeleton) {
         this.spawnAtLocation(Item.record_01.id + this.random.nextInt(2), 1);
      }

   }

   protected void checkHurtTarget(Entity var1, float var2) {
      int var3 = this.getSwellDir();
      if (var3 <= 0 && var2 < 3.0F || var3 > 0 && var2 < 7.0F) {
         if (this.swell == 0) {
            this.level.playSound(this, "random.fuse", 1.0F, 0.5F);
         }

         this.setSwellDir(1);
         ++this.swell;
         if (this.swell >= 30) {
            this.level.explode(this, this.x, this.y, this.z, 3.0F);
            this.remove();
         }

         this.holdGround = true;
      } else {
         this.setSwellDir(-1);
         --this.swell;
         if (this.swell < 0) {
            this.swell = 0;
         }
      }

   }

   public float getSwelling(float var1) {
      return ((float)this.oldSwell + (float)(this.swell - this.oldSwell) * var1) / 28.0F;
   }

   protected int getDeathLoot() {
      return Item.sulphur.id;
   }

   private int getSwellDir() {
      return this.entityData.getByte(16);
   }

   private void setSwellDir(int var1) {
      this.entityData.set(16, (byte)var1);
   }
}
