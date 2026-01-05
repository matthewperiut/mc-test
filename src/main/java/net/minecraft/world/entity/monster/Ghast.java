package net.minecraft.world.entity.monster;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import util.Mth;

public class Ghast extends FlyingMob implements Enemy {
   public int floatDuration = 0;
   public double xTarget;
   public double yTarget;
   public double zTarget;
   private Entity target = null;
   private int retargetTime = 0;
   public int oCharge = 0;
   public int charge = 0;

   public Ghast(Level var1) {
      super(var1);
      this.textureName = "/mob/ghast.png";
      this.setSize(4.0F, 4.0F);
      this.fireImmune = true;
   }

   protected void updateAi() {
      if (this.level.difficulty == 0) {
         this.remove();
      }

      this.oCharge = this.charge;
      double var1 = this.xTarget - this.x;
      double var3 = this.yTarget - this.y;
      double var5 = this.zTarget - this.z;
      double var7 = (double)Mth.sqrt(var1 * var1 + var3 * var3 + var5 * var5);
      if (var7 < (double)1.0F || var7 > (double)60.0F) {
         this.xTarget = this.x + (double)((this.random.nextFloat() * 2.0F - 1.0F) * 16.0F);
         this.yTarget = this.y + (double)((this.random.nextFloat() * 2.0F - 1.0F) * 16.0F);
         this.zTarget = this.z + (double)((this.random.nextFloat() * 2.0F - 1.0F) * 16.0F);
      }

      if (this.floatDuration-- <= 0) {
         this.floatDuration += this.random.nextInt(5) + 2;
         if (this.canReach(this.xTarget, this.yTarget, this.zTarget, var7)) {
            this.xd += var1 / var7 * 0.1;
            this.yd += var3 / var7 * 0.1;
            this.zd += var5 / var7 * 0.1;
         } else {
            this.xTarget = this.x;
            this.yTarget = this.y;
            this.zTarget = this.z;
         }
      }

      if (this.target != null && this.target.removed) {
         this.target = null;
      }

      if (this.target == null || this.retargetTime-- <= 0) {
         this.target = this.level.getNearestPlayer(this, (double)100.0F);
         if (this.target != null) {
            this.retargetTime = 20;
         }
      }

      double var9 = (double)64.0F;
      if (this.target != null && this.target.distanceToSqr(this) < var9 * var9) {
         double var11 = this.target.x - this.x;
         double var13 = this.target.bb.y0 + (double)(this.target.bbHeight / 2.0F) - (this.y + (double)(this.bbHeight / 2.0F));
         double var15 = this.target.z - this.z;
         this.yBodyRot = this.yRot = -((float)Math.atan2(var11, var15)) * 180.0F / (float)Math.PI;
         if (this.canSee(this.target)) {
            if (this.charge == 10) {
               this.level.playSound(this, "mob.ghast.charge", this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
            }

            ++this.charge;
            if (this.charge == 20) {
               this.level.playSound(this, "mob.ghast.fireball", this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
               Fireball var17 = new Fireball(this.level, this, var11, var13, var15);
               double var18 = (double)4.0F;
               Vec3 var20 = this.getViewVector(1.0F);
               var17.x = this.x + var20.x * var18;
               var17.y = this.y + (double)(this.bbHeight / 2.0F) + (double)0.5F;
               var17.z = this.z + var20.z * var18;
               this.level.addEntity(var17);
               this.charge = -40;
            }
         } else if (this.charge > 0) {
            --this.charge;
         }
      } else {
         this.yBodyRot = this.yRot = -((float)Math.atan2(this.xd, this.zd)) * 180.0F / (float)Math.PI;
         if (this.charge > 0) {
            --this.charge;
         }
      }

      this.textureName = this.charge > 10 ? "/mob/ghast_fire.png" : "/mob/ghast.png";
   }

   private boolean canReach(double var1, double var3, double var5, double var7) {
      double var9 = (this.xTarget - this.x) / var7;
      double var11 = (this.yTarget - this.y) / var7;
      double var13 = (this.zTarget - this.z) / var7;
      AABB var15 = this.bb.copy();

      for(int var16 = 1; (double)var16 < var7; ++var16) {
         var15.move(var9, var11, var13);
         if (this.level.getCubes(this, var15).size() > 0) {
            return false;
         }
      }

      return true;
   }

   protected String getAmbientSound() {
      return "mob.ghast.moan";
   }

   protected String getHurtSound() {
      return "mob.ghast.scream";
   }

   protected String getDeathSound() {
      return "mob.ghast.death";
   }

   protected void checkHurtTarget(Entity var1, float var2) {
   }

   protected int getDeathLoot() {
      return Item.sulphur.id;
   }

   protected float getSoundVolume() {
      return 10.0F;
   }

   public boolean canSpawn() {
      return this.random.nextInt(20) == 0 && super.canSpawn() && this.level.difficulty > 0;
   }

   public int getMaxSpawnClusterSize() {
      return 1;
   }
}
