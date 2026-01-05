package net.minecraft.world.entity.projectile;

import com.mojang.nbt.CompoundTag;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import util.Mth;

public class Fireball extends Entity {
   private int xTile = -1;
   private int yTile = -1;
   private int zTile = -1;
   private int lastTile = 0;
   private boolean inGround = false;
   public int shakeTime = 0;
   private Mob owner;
   private int life;
   private int flightTime = 0;
   public double xPower;
   public double yPower;
   public double zPower;

   public Fireball(Level var1) {
      super(var1);
      this.setSize(1.0F, 1.0F);
   }

   protected void defineSynchedData() {
   }

   public boolean shouldRenderAtSqrDistance(double var1) {
      double var3 = this.bb.getSize() * (double)4.0F;
      var3 *= (double)64.0F;
      return var1 < var3 * var3;
   }

   public Fireball(Level var1, Mob var2, double var3, double var5, double var7) {
      super(var1);
      this.owner = var2;
      this.setSize(1.0F, 1.0F);
      this.moveTo(var2.x, var2.y, var2.z, var2.yRot, var2.xRot);
      this.setPos(this.x, this.y, this.z);
      this.heightOffset = 0.0F;
      this.xd = this.yd = this.zd = (double)0.0F;
      var3 += this.random.nextGaussian() * 0.4;
      var5 += this.random.nextGaussian() * 0.4;
      var7 += this.random.nextGaussian() * 0.4;
      double var9 = (double)Mth.sqrt(var3 * var3 + var5 * var5 + var7 * var7);
      this.xPower = var3 / var9 * 0.1;
      this.yPower = var5 / var9 * 0.1;
      this.zPower = var7 / var9 * 0.1;
   }

   public void tick() {
      super.tick();
      this.onFire = 10;
      if (this.shakeTime > 0) {
         --this.shakeTime;
      }

      if (this.inGround) {
         int var1 = this.level.getTile(this.xTile, this.yTile, this.zTile);
         if (var1 == this.lastTile) {
            ++this.life;
            if (this.life == 1200) {
               this.remove();
            }

            return;
         }

         this.inGround = false;
         this.xd *= (double)(this.random.nextFloat() * 0.2F);
         this.yd *= (double)(this.random.nextFloat() * 0.2F);
         this.zd *= (double)(this.random.nextFloat() * 0.2F);
         this.life = 0;
         this.flightTime = 0;
      } else {
         ++this.flightTime;
      }

      Vec3 var15 = Vec3.newTemp(this.x, this.y, this.z);
      Vec3 var2 = Vec3.newTemp(this.x + this.xd, this.y + this.yd, this.z + this.zd);
      HitResult var3 = this.level.clip(var15, var2);
      var15 = Vec3.newTemp(this.x, this.y, this.z);
      var2 = Vec3.newTemp(this.x + this.xd, this.y + this.yd, this.z + this.zd);
      if (var3 != null) {
         var2 = Vec3.newTemp(var3.pos.x, var3.pos.y, var3.pos.z);
      }

      Entity var4 = null;
      List var5 = this.level.getEntities(this, this.bb.expand(this.xd, this.yd, this.zd).grow((double)1.0F, (double)1.0F, (double)1.0F));
      double var6 = (double)0.0F;

      for(int var8 = 0; var8 < var5.size(); ++var8) {
         Entity var9 = (Entity)var5.get(var8);
         if (var9.isPickable() && (var9 != this.owner || this.flightTime >= 25)) {
            float var10 = 0.3F;
            AABB var11 = var9.bb.grow((double)var10, (double)var10, (double)var10);
            HitResult var12 = var11.clip(var15, var2);
            if (var12 != null) {
               double var13 = var15.distanceTo(var12.pos);
               if (var13 < var6 || var6 == (double)0.0F) {
                  var4 = var9;
                  var6 = var13;
               }
            }
         }
      }

      if (var4 != null) {
         var3 = new HitResult(var4);
      }

      if (var3 != null) {
         if (var3.entity != null && var3.entity.hurt(this.owner, 0)) {
         }

         this.level.explode((Entity)null, this.x, this.y, this.z, 1.0F, true);
         this.remove();
      }

      this.x += this.xd;
      this.y += this.yd;
      this.z += this.zd;
      float var18 = Mth.sqrt(this.xd * this.xd + this.zd * this.zd);
      this.yRot = (float)(Math.atan2(this.xd, this.zd) * (double)180.0F / (double)(float)Math.PI);

      for(this.xRot = (float)(Math.atan2(this.yd, (double)var18) * (double)180.0F / (double)(float)Math.PI); this.xRot - this.xRotO < -180.0F; this.xRotO -= 360.0F) {
      }

      while(this.xRot - this.xRotO >= 180.0F) {
         this.xRotO += 360.0F;
      }

      while(this.yRot - this.yRotO < -180.0F) {
         this.yRotO -= 360.0F;
      }

      while(this.yRot - this.yRotO >= 180.0F) {
         this.yRotO += 360.0F;
      }

      this.xRot = this.xRotO + (this.xRot - this.xRotO) * 0.2F;
      this.yRot = this.yRotO + (this.yRot - this.yRotO) * 0.2F;
      float var19 = 0.95F;
      if (this.isInWater()) {
         for(int var20 = 0; var20 < 4; ++var20) {
            float var21 = 0.25F;
            this.level.addParticle("bubble", this.x - this.xd * (double)var21, this.y - this.yd * (double)var21, this.z - this.zd * (double)var21, this.xd, this.yd, this.zd);
         }

         var19 = 0.8F;
      }

      this.xd += this.xPower;
      this.yd += this.yPower;
      this.zd += this.zPower;
      this.xd *= (double)var19;
      this.yd *= (double)var19;
      this.zd *= (double)var19;
      this.level.addParticle("smoke", this.x, this.y + (double)0.5F, this.z, (double)0.0F, (double)0.0F, (double)0.0F);
      this.setPos(this.x, this.y, this.z);
   }

   public void addAdditonalSaveData(CompoundTag var1) {
      var1.putShort("xTile", (short)this.xTile);
      var1.putShort("yTile", (short)this.yTile);
      var1.putShort("zTile", (short)this.zTile);
      var1.putByte("inTile", (byte)this.lastTile);
      var1.putByte("shake", (byte)this.shakeTime);
      var1.putByte("inGround", (byte)(this.inGround ? 1 : 0));
   }

   public void readAdditionalSaveData(CompoundTag var1) {
      this.xTile = var1.getShort("xTile");
      this.yTile = var1.getShort("yTile");
      this.zTile = var1.getShort("zTile");
      this.lastTile = var1.getByte("inTile") & 255;
      this.shakeTime = var1.getByte("shake") & 255;
      this.inGround = var1.getByte("inGround") == 1;
   }

   public boolean isPickable() {
      return true;
   }

   public float getPickRadius() {
      return 1.0F;
   }

   public boolean hurt(Entity var1, int var2) {
      this.markHurt();
      if (var1 != null) {
         Vec3 var3 = var1.getLookAngle();
         if (var3 != null) {
            this.xd = var3.x;
            this.yd = var3.y;
            this.zd = var3.z;
            this.xPower = this.xd * 0.1;
            this.yPower = this.yd * 0.1;
            this.zPower = this.zd * 0.1;
         }

         return true;
      } else {
         return false;
      }
   }

   public float getShadowHeightOffs() {
      return 0.0F;
   }
}
