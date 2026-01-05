package net.minecraft.world.entity;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import util.Mth;

public class PathfinderMob extends Mob {
   private static final int MAX_TURN = 30;
   private Path path;
   protected Entity attackTarget;
   protected boolean holdGround = false;

   public PathfinderMob(Level var1) {
      super(var1);
   }

   protected void updateAi() {
      this.holdGround = false;
      float var1 = 16.0F;
      if (this.attackTarget == null) {
         this.attackTarget = this.findAttackTarget();
         if (this.attackTarget != null) {
            this.path = this.level.findPath(this, this.attackTarget, var1);
         }
      } else if (!this.attackTarget.isAlive()) {
         this.attackTarget = null;
      } else {
         float var2 = this.attackTarget.distanceTo(this);
         if (this.canSee(this.attackTarget)) {
            this.checkHurtTarget(this.attackTarget, var2);
         }
      }

      if (this.holdGround || this.attackTarget == null || this.path != null && this.random.nextInt(20) != 0) {
         if (this.path == null && this.random.nextInt(80) == 0 || this.random.nextInt(80) == 0) {
            boolean var21 = false;
            int var3 = -1;
            int var4 = -1;
            int var5 = -1;
            float var6 = -99999.0F;

            for(int var7 = 0; var7 < 10; ++var7) {
               int var8 = Mth.floor(this.x + (double)this.random.nextInt(13) - (double)6.0F);
               int var9 = Mth.floor(this.y + (double)this.random.nextInt(7) - (double)3.0F);
               int var10 = Mth.floor(this.z + (double)this.random.nextInt(13) - (double)6.0F);
               float var11 = this.getWalkTargetValue(var8, var9, var10);
               if (var11 > var6) {
                  var6 = var11;
                  var3 = var8;
                  var4 = var9;
                  var5 = var10;
                  var21 = true;
               }
            }

            if (var21) {
               this.path = this.level.findPath(this, var3, var4, var5, 10.0F);
            }
         }
      } else {
         this.path = this.level.findPath(this, this.attackTarget, var1);
      }

      int var22 = Mth.floor(this.bb.y0);
      boolean var23 = this.isInWater();
      boolean var24 = this.isInLava();
      this.xRot = 0.0F;
      if (this.path != null && this.random.nextInt(100) != 0) {
         Vec3 var25 = this.path.current(this);
         double var26 = (double)(this.bbWidth * 2.0F);

         while(var25 != null && var25.distanceToSqr(this.x, var25.y, this.z) < var26 * var26) {
            this.path.next();
            if (this.path.isDone()) {
               var25 = null;
               this.path = null;
            } else {
               var25 = this.path.current(this);
            }
         }

         this.jumping = false;
         if (var25 != null) {
            double var27 = var25.x - this.x;
            double var28 = var25.z - this.z;
            double var12 = var25.y - (double)var22;
            float var14 = (float)(Math.atan2(var28, var27) * (double)180.0F / (double)(float)Math.PI) - 90.0F;
            float var15 = var14 - this.yRot;

            for(this.yya = this.runSpeed; var15 < -180.0F; var15 += 360.0F) {
            }

            while(var15 >= 180.0F) {
               var15 -= 360.0F;
            }

            if (var15 > 30.0F) {
               var15 = 30.0F;
            }

            if (var15 < -30.0F) {
               var15 = -30.0F;
            }

            this.yRot += var15;
            if (this.holdGround && this.attackTarget != null) {
               double var16 = this.attackTarget.x - this.x;
               double var18 = this.attackTarget.z - this.z;
               float var20 = this.yRot;
               this.yRot = (float)(Math.atan2(var18, var16) * (double)180.0F / (double)(float)Math.PI) - 90.0F;
               var15 = (var20 - this.yRot + 90.0F) * (float)Math.PI / 180.0F;
               this.xxa = -Mth.sin(var15) * this.yya * 1.0F;
               this.yya = Mth.cos(var15) * this.yya * 1.0F;
            }

            if (var12 > (double)0.0F) {
               this.jumping = true;
            }
         }

         if (this.attackTarget != null) {
            this.lookAt(this.attackTarget, 30.0F);
         }

         if (this.horizontalCollision) {
            this.jumping = true;
         }

         if (this.random.nextFloat() < 0.8F && (var23 || var24)) {
            this.jumping = true;
         }

      } else {
         super.updateAi();
         this.path = null;
      }
   }

   protected void checkHurtTarget(Entity var1, float var2) {
   }

   protected float getWalkTargetValue(int var1, int var2, int var3) {
      return 0.0F;
   }

   protected Entity findAttackTarget() {
      return null;
   }

   public boolean canSpawn() {
      int var1 = Mth.floor(this.x);
      int var2 = Mth.floor(this.bb.y0);
      int var3 = Mth.floor(this.z);
      return super.canSpawn() && this.getWalkTargetValue(var1, var2, var3) >= 0.0F;
   }
}
