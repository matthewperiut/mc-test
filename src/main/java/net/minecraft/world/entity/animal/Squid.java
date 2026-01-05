package net.minecraft.world.entity.animal;

import com.mojang.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import util.Mth;

public class Squid extends WaterAnimal {
   public float xBodyRot = 0.0F;
   public float xBodyRotO = 0.0F;
   public float zBodyRot = 0.0F;
   public float zBodyRotO = 0.0F;
   public float tentacleMovement = 0.0F;
   public float oldTentacleMovement = 0.0F;
   public float tentacleAngle = 0.0F;
   public float oldTentacleAngle = 0.0F;
   private float speed = 0.0F;
   private float tentacleSpeed = 0.0F;
   private float rotateSpeed = 0.0F;
   private float tx = 0.0F;
   private float ty = 0.0F;
   private float tz = 0.0F;

   public Squid(Level var1) {
      super(var1);
      this.textureName = "/mob/squid.png";
      this.setSize(0.95F, 0.95F);
      this.tentacleSpeed = 1.0F / (this.random.nextFloat() + 1.0F) * 0.2F;
   }

   public void addAdditonalSaveData(CompoundTag var1) {
      super.addAdditonalSaveData(var1);
   }

   public void readAdditionalSaveData(CompoundTag var1) {
      super.readAdditionalSaveData(var1);
   }

   protected String getAmbientSound() {
      return null;
   }

   protected String getHurtSound() {
      return null;
   }

   protected String getDeathSound() {
      return null;
   }

   protected float getSoundVolume() {
      return 0.4F;
   }

   protected int getDeathLoot() {
      return 0;
   }

   protected void dropDeathLoot() {
      int var1 = this.random.nextInt(3) + 1;

      for(int var2 = 0; var2 < var1; ++var2) {
         this.spawnAtLocation(new ItemInstance(Item.dye_powder, 1, 0), 0.0F);
      }

   }

   public boolean interact(Player var1) {
      ItemInstance var2 = var1.inventory.getSelected();
      if (var2 != null && var2.id == Item.bucket_empty.id) {
         var1.inventory.setItem(var1.inventory.selected, new ItemInstance(Item.milk));
         return true;
      } else {
         return false;
      }
   }

   public boolean isInWater() {
      return this.level.checkAndHandleWater(this.bb.grow((double)0.0F, (double)-0.6F, (double)0.0F), Material.water, this);
   }

   public void aiStep() {
      super.aiStep();
      this.xBodyRotO = this.xBodyRot;
      this.zBodyRotO = this.zBodyRot;
      this.oldTentacleMovement = this.tentacleMovement;
      this.oldTentacleAngle = this.tentacleAngle;
      this.tentacleMovement += this.tentacleSpeed;
      if (this.tentacleMovement > ((float)Math.PI * 2F)) {
         this.tentacleMovement -= ((float)Math.PI * 2F);
         if (this.random.nextInt(10) == 0) {
            this.tentacleSpeed = 1.0F / (this.random.nextFloat() + 1.0F) * 0.2F;
         }
      }

      if (this.isInWater()) {
         if (this.tentacleMovement < (float)Math.PI) {
            float var1 = this.tentacleMovement / (float)Math.PI;
            this.tentacleAngle = Mth.sin(var1 * var1 * (float)Math.PI) * (float)Math.PI * 0.25F;
            if ((double)var1 > (double)0.75F) {
               this.speed = 1.0F;
               this.rotateSpeed = 1.0F;
            } else {
               this.rotateSpeed *= 0.8F;
            }
         } else {
            this.tentacleAngle = 0.0F;
            this.speed *= 0.9F;
            this.rotateSpeed *= 0.99F;
         }

         if (!this.interpolateOnly) {
            this.xd = (double)(this.tx * this.speed);
            this.yd = (double)(this.ty * this.speed);
            this.zd = (double)(this.tz * this.speed);
         }

         float var2 = Mth.sqrt(this.xd * this.xd + this.zd * this.zd);
         this.yBodyRot += (-((float)Math.atan2(this.xd, this.zd)) * 180.0F / (float)Math.PI - this.yBodyRot) * 0.1F;
         this.yRot = this.yBodyRot;
         this.zBodyRot += (float)Math.PI * this.rotateSpeed * 1.5F;
         this.xBodyRot += (-((float)Math.atan2((double)var2, this.yd)) * 180.0F / (float)Math.PI - this.xBodyRot) * 0.1F;
      } else {
         this.tentacleAngle = Mth.abs(Mth.sin(this.tentacleMovement)) * (float)Math.PI * 0.25F;
         if (!this.interpolateOnly) {
            this.xd = (double)0.0F;
            this.yd -= 0.08;
            this.yd *= (double)0.98F;
            this.zd = (double)0.0F;
         }

         this.xBodyRot = (float)((double)this.xBodyRot + (double)(-90.0F - this.xBodyRot) * 0.02);
      }

   }

   public void travel(float var1, float var2) {
      this.move(this.xd, this.yd, this.zd);
   }

   protected void updateAi() {
      if (this.random.nextInt(50) == 0 || !this.wasInWater || this.tx == 0.0F && this.ty == 0.0F && this.tz == 0.0F) {
         float var1 = this.random.nextFloat() * (float)Math.PI * 2.0F;
         this.tx = Mth.cos(var1) * 0.2F;
         this.ty = -0.1F + this.random.nextFloat() * 0.2F;
         this.tz = Mth.sin(var1) * 0.2F;
      }

   }
}
